
package de.voodoosoft.blackcat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;



/**
 * Supplies components and injects defined dependencies.
 * <p/>
 * Component classes must be previously be registered by calling {@link #addComponent} and have a default constructor.
 * <br/>Dependencies are marked with {@link Inject} field annotations.
 * Each dependency type must have its own {@link Provider}.
 * <br/><code>Injector</code> is thread safe.
 * <p/>Example:
 * <pre>{@code
 * public class Band {
 *    @literal@Inject
 *    private Bass bass;
 *
 *    public Band() {
 *    }
 * }
 *
 * Injector.getInjector().addComponent(Bass.class, new Provider<Bass>() {
 *    @literal@Override
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
 * <pre>{@code
 * public class Band {
 *    @literal@Inject("Precision")
 *    private Bass bass;
 *
 *    public Band() {
 *    }
 * }
 *
 * public class BassProvider implements Provider<Bass> {
 *    @literal@Override
 *    public Bass provide() {
 *       return new Bass();
 *    }
 * }
 *
 * Injector.getInjector().addComponent(Bass.class, "Precision", new BassProvider());
 * Bass bass = Injector.getInjector().get(Bass.class);
 * }
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
		componentEntries = new HashMap<>();
		providerEntries = new ArrayList<>();
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
	 * @see #get(Class)
	 *
	 * @param type component class
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> void addComponent(Class<T> type, Provider<T> provider) {
		addComponent(type, (String)null);
		addProvider(type, null, provider);
	}

	/**
	 * Registers a named component.
	 * <p/>Named componentEntries are mainly used for resolving named dependencies.
	 *
	 * @see #getComponent(Class)
	 * @see #get(Class)
	 * @see Inject
	 *
	 * @param type component class
	 * @param name dependency name
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> void addComponent(Class<T> type, String name, Provider<T> provider) {
		addComponent(type, name);
		addProvider(type, name, provider);
	}

	/**
	 * Returns an object for the given type.
	 *
	 * @see #get(Class)
	 * @see #getComponent(Class, String)
	 *
	 * @param type
	 * @param <T>
	 * @return
	 */
	public <T> T getComponent(Class<T> type) {
		return getComponent(type, null);
	}

	/**
	 * Returns an object for the given type and name.
	 *
	 * @see #get(Class)
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
		Provider<T> provider = getProvider(type, name);
		if (provider == null) {
			throw new RuntimeException("component [" + type + "] named [" + name + "] needs a provider");
		}
		T component = provider.provide();
		injectDependencies(component, componentEntry);

		return component;
	}

	/**
	 * Convenience method for the global injector that returns an object of the given class with injected dependencies.
	 *
	 * @see #getComponent(Class)
	 *
	 * @param type component class
	 * @param <T> component type
	 * @return
	 */
	public static <T> T get(Class<T> type) {
		return Holder.injector.getComponent(type);
	}

	private void addComponent(Class type, String name) {
		// prevent duplicates
		if (getComponentEntry(type, name) != null) {
			throw new RuntimeException("duplicate component [" + type + "] named [" + name + "]");
		}

		// collect component meta data
		ComponentEntry componentEntry = new ComponentEntry(type, name);
		Class c = type;
		while(c != Object.class && componentEntry.getPostConstruct() == null) {
			Method[] methods = c.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
				if (postConstruct != null) {
					componentEntry.setPostConstruct(method);
					break;
				}
			}
			c = c.getSuperclass();
		}
		synchronized (componentEntries) {
			componentEntries.put(componentEntry, componentEntry);
		}
	}

	/**
	 * Registers a dependency provider that supplies dependencies by class and name.
	 *
	 * @param type dependency class to provide
	 * @param name dependency name
	 * @param provider dependency provider
	 * @param <T> dependency type
	 */
	private <T> void addProvider(Class<T> type, String name, Provider<T> provider) {
		synchronized (providerEntries) {
			// look for duplicates
			int size = providerEntries.size();
			for (int i = 0; i < size; i++) {
				ProviderEntry provKey = providerEntries.get(i);
				if (type.equals(provKey.getType()) && Objects.equals(name, provKey.getName())) {
					throw new RuntimeException("duplicate provider for type [" + type + "] named [" + name + "]");
				}
			}

			ProviderEntry providerEntry = new ProviderEntry(provider, type, name);
			providerEntries.add(providerEntry);
		}
	}

	private <T> void injectDependencies(T component, ComponentEntry componentEntry) {
		// lazily collect fields to inject
		List<Injection> injections;
		synchronized (componentEntry.getLock()) {
			injections = componentEntry.getInjections();
			if (injections == null) {
				injections = collectInjections(component);
				componentEntry.setInjections(injections);
			}
		}

		// inject field values
		int size = injections.size();
		for (int i = 0; i < size; i++) {
			Injection injection = injections.get(i);
			Provider provider = getProvider(injection.getType(), injection.getName());
			if (provider == null) {
				throw new RuntimeException("no provider for injection on [" + injection.getField() + "] typed [" + injection.getType() + "] named [" + injection.getName() + "] of component [" + componentEntry.getType() + "]");
			}

			// recursively create injections
			try {
				Object injectionValue = provider.provide();
				Field field = injection.getHolderType().getDeclaredField(injection.getField());
				ComponentEntry nestedComponent = getComponentEntry(injection.getType(), injection.getName());
				if (nestedComponent != null) {
					injectDependencies(injectionValue, nestedComponent);
				}

				field.setAccessible(true);
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

	private <T> List<Injection> collectInjections(T component) {
		List<Injection> injections = new ArrayList<>();

		Class c = component.getClass();
		while(c != Object.class) {
			Field[] fields = c.getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				Inject injectAnnotation = field.getAnnotation(Inject.class);
				if (injectAnnotation != null) {
					String value = injectAnnotation.value();
					if ("".equals(value)) {
						value = null;
					}
					Injection injection = new Injection(c, field.getType(), field.getName(), value);
					injections.add(injection);
				}
			}
			c = c.getSuperclass();
		}

		return injections;
	}

	private <T> Provider<T> getProvider(Class type, String name) {
		synchronized (providerEntries) {
			int size = providerEntries.size();
			if (name == null || "".equals(name)) {
				// 1. look for exact match
				for (int i = 0; i < size; i++) {
					ProviderEntry provKey = providerEntries.get(i);
					if (provKey.getName() == null && type.equals(provKey.getType())) {
						return provKey.getProvider();
					}
				}

				// 2. look for descendants
				for (int i = 0; i < size; i++) {
					ProviderEntry provKey = providerEntries.get(i);
					if (provKey.getName() == null && type.isAssignableFrom(provKey.getType())) {
						return provKey.getProvider();
					}
				}
			}
			else {
				for (int i = 0; i < size; i++) {
					ProviderEntry provKey = providerEntries.get(i);
					if (type.isAssignableFrom(provKey.getType()) && Objects.equals(name, provKey.getName())) {
						return provKey.getProvider();
					}
				}
			}
		}
		return null;
	}

	private ComponentEntry getComponentEntry(Class type, String name) {
		ComponentEntry entryLookup = threadLocalLookup.get();
		if (entryLookup == null) {
			entryLookup = new ComponentEntry();
			threadLocalLookup.set(entryLookup);
		}
		entryLookup.setName(name);
		entryLookup.setType(type);

		synchronized (componentEntries) {
			ComponentEntry componentEntry = componentEntries.get(entryLookup);
			return componentEntry;
		}
	}

	private Map<ComponentEntry,ComponentEntry> componentEntries;
	private List<ProviderEntry> providerEntries;
	private ThreadLocal<ComponentEntry> threadLocalLookup = new ThreadLocal<>();
}
