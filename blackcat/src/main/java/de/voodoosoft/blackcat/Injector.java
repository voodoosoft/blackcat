
package de.voodoosoft.blackcat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Supplies components and injects defined dependencies.
 * <p/>
 * Component classes must be previously be registered by calling {@link #defineComponent} and have a default constructor.
 * <br/>Dependencies are marked with {@link Inject} field annotations.
 * <br/>Dependency injections can be defined recursively.  
 * <br/>Components should not be defined from multiple threads at the same time, but may be requested concurrently.
 * <br/>The {@link PostConstruct} annotation can be used for additional initialization after objects have been created.
 *
 * <p/>Example:
 * <pre>
 * {@code
 * public class Band {
 *    {@literal @}Inject
 *    private Bass bass;
 *
 *    public Band() {
 *    }
 * }
 *
 * Injector.getInjector().defineComponent(Bass.class, new Provider<Bass>() {
 *    {@literal @}Override
 *    public Bass provide() {
 *       return new Bass();
 *    }
 * });
 *
 * Bass bass = Injector.getInjector().get(Bass.class);
 * }
 * </pre>
 * Naturally, providers do not need to be defined with anonymous classes.
 * <br/>Dependencies can additionally be identified by assigning a name:
 * <br/>
 * <pre>
 * {@code
 * public class Band {
 *    {@literal @}Inject("Precision")
 *    private Bass bass;
 *
 *    public Band() {
 *    }
 * }
 *
 * public class BassProvider implements Provider<Bass> {
 *    {@literal @}Override
 *    public Bass provide() {
 *       return new Bass();
 *    }
 * }
 *
 * Injector.getInjector().defineComponent(Bass.class, "Precision", new BassProvider());
 * Bass bass = Injector.getInjector().get(Bass.class);
 * }
 *
 * @see Provider
 * @see Inject
 */
public class Injector {
	private static final class Holder {
		static final Injector injector = new Injector();
	}

	/**
	 * Creates a new injector.
	 */
	public Injector() {
		components = new HashMap<>();
		componentsByType = new HashMap<>();
	}

	/**
	 * Returns the convenience global injector.
	 *
	 * <br/>Note that <code>Injector</code> is not a singleton.
	 *
	 * @return global injector
	 */
	public static Injector getInjector() {
		return Holder.injector;
	}

	/**
	 * Adds the given class to the list of managed classes that will get injected dependencies.
	 *
	 * @see #getComponent(Class)
	 *
	 * @param type component class
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> void defineComponent(Class<T> type, Provider<T> provider) {
		doDefineComponent(type, (String)null, provider);
	}

	/**
	 * Registers a named component.
	 * <p/>Named componentEntries are mainly used for resolving named dependencies.
	 *
	 * @see #getComponent(Class, String)
	 *
	 * @param type component class
	 * @param name dependency name
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> void defineComponent(Class<T> type, String name, Provider<T> provider) {
		doDefineComponent(type, name, provider);
	}

	/**
	 * Adds the given class to the list of managed classes and returns one created object as well.
	 *
	 * @param type component class
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> T defineAndGetComponent(Class<T> type, Provider<T> provider) {
		doDefineComponent(type, (String)null, provider);
		T component = getComponent(type);

		return component;
	}

	/**
	 * Returns an object for the given type.
	 *
	 * @see #getComponent(Class, String)
	 *
	 * @param type
	 * @param <T>
	 * @return
	 */
	public <T> T getComponent(Class<T> type) {
		return getComponent(type, (String)null);
	}

	/**
	 * Returns an object for the given type and name.
	 *
	 * @see #getComponent(Class)
	 *
	 * @param type component class
	 * @param <T> component type
	 * @return
	 */
	public <T> T getComponent(Class<T> type, String name) {
		// look up component
		ComponentEntry componentEntry = getComponentEntry(type, name);
		if (componentEntry == null) {
			throw new RuntimeException("unknown component [" + type + "] named [" + name + "]");
		}

		// build component with dependencies
		Provider<T> provider = (Provider<T>)componentEntry.getProvider();
		T component = provider.provide();
		if (component != null) {
			injectDependencies(component, componentEntry);
		}

		return component;
	}

	private void doDefineComponent(Class<?> type, String name, Provider provider) {
		// prevent duplicates
		if (getComponentEntry(type, name) != null) {
			throw new RuntimeException("duplicate component [" + type + "] named [" + name + "]");
		}

		// collect component meta data
		ComponentEntry componentEntry = new ComponentEntry(type, name, provider);
		List<Injection> injections = componentEntry.getInjections();
		Class<?> c = type;
		while(c != null && c != Object.class) {
			// collect PostConstruct methods
			Method[] methods = c.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
				if (postConstruct != null) {
					componentEntry.setPostConstruct(method);
					break;
				}
			}

			// collect Inject fields
			Field[] fields = c.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				Inject injectAnnotation = field.getAnnotation(Inject.class);
				if (injectAnnotation != null) {
					String injectionName = injectAnnotation.value();
					if ("".equals(injectionName)) {
						injectionName = null;
					}
					Injection injection = new Injection(field, injectionName);
					field.setAccessible(true);
					injections.add(injection);
				}
			}

			c = c.getSuperclass();
		}

		components.put(componentEntry, componentEntry);
		if (name == null) {
			componentsByType.put(type, componentEntry);
		}
	}

	private <T> void injectDependencies(T component, ComponentEntry componentEntry) {
		// inject field values
		List<Injection> injections = componentEntry.getInjections();
		int size = injections.size();
		for (int i = 0; i < size; i++) {
			Injection injection = injections.get(i);
			Class<?> injectionType = injection.getField().getType();
			ComponentEntry injectionEntry = getComponentEntry(injectionType, injection.getName());
			if (injectionEntry == null) {
				throw new RuntimeException("no component of type [" + injectionType + "] defined for injection into [" + componentEntry.getType() + "]");
			}
			Provider<?> provider = injectionEntry.getProvider();

			// recursively create injections
			try {
				Object injectionValue = provider.provide();
				Field field = injection.getField();
				ComponentEntry nestedComponent = getComponentEntry(injectionType, injection.getName());
				if (nestedComponent != null) {
					injectDependencies(injectionValue, nestedComponent);
				}
				field.set(component, injectionValue);
			}
			catch (Exception e) {
				throw new RuntimeException("injectDependencies", e);
			}
		}

		// optionally invoke post construction
		Method postConstruct = componentEntry.getPostConstruct();
		if (componentEntry != null && postConstruct != null) {
			try {
				postConstruct.setAccessible(true);
				postConstruct.invoke(component);
			}
			catch (Exception e) {
				throw new RuntimeException("getComponent", e);
			}
		}
	}

	private ComponentEntry getComponentEntry(Class<?> type, String name) {
		// find named component
		if (name != null) {
			ComponentEntry entryLookup = componentLookup.get();
			if (entryLookup == null) {
				entryLookup = new ComponentEntry();
				componentLookup.set(entryLookup);
			}
			entryLookup.setName(name);
			entryLookup.setType(type);
			return components.get(entryLookup);
		}

		// find unnamed component
		ComponentEntry componentEntry = componentsByType.get(type);
		if (componentEntry != null) {
			return componentEntry;
		}

		// find ancestor definition
		for (ComponentEntry entry : components.values()) {
			if (entry.getName() == null && type.isAssignableFrom(entry.getType())) {
				return entry;
			}
		}

		return null;
	}

	private Map<ComponentEntry, ComponentEntry> components;
	private Map<Class<?>, ComponentEntry> componentsByType;
	private ThreadLocal<ComponentEntry> componentLookup = new ThreadLocal<>();
}
