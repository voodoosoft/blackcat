
package de.voodoosoft.blackcat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Supplies components and injects defined dependencies.
 * <p/>
 * Component classes must be previously be registered by calling {@link #defineComponent} and have a default constructor.
 * <br/>Dependencies are marked with {@link Inject} field annotations.
 * <br/>Dependency injections can be defined recursively.
 * <br/>Components should not be defined from multiple threads, but maybe requested concurrently.
 * <br/>All components should have been defined before starting to request components. 
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
		unnamedComponentsByType = new HashMap<>();
		componentsByName = new HashMap<>();
		unnamedComponents = new ArrayList<>();
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

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	/**
	 * Adds the given class to the list of managed classes that will get injected dependencies.
	 * <br/>Only class members of the given type and its ancestors are analyzed as potential injection points
	 * which means that usually concrete classes have to be registered as components and not interfaces.
	 * <br/>However, for injections points interfaces can be utilized as well.
	 *
	 * @see #getComponent(Class)
	 *
	 * @param type component class
	 * @param singleton true if only one object of the given class will ever created
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> void defineComponent(Class<T> type, boolean singleton, Provider<T> provider) {
		doDefineComponent(type, null, provider, singleton);
	}

	public <T> void defineComponent(Class<T> type, Provider<T> provider) {
		doDefineComponent(type, null, provider, false);
	}

	/**
	 * Registers a named component.
	 * <p/>Named componentEntries are mainly used for resolving named dependencies.
	 * <br/>Only class members of the given type and its ancestors are analyzed as potential injection points
	 * which means that usually concrete classes have to be registered as components and not interfaces.
	 * <br/>However, for injections points interfaces can be utilized as well.
	 *
	 * @see #getComponent(Class, String)
	 *
	 * @param type component class
	 * @param name dependency name
  	 * @param singleton true if only one object of the given class will ever created
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> void defineComponent(Class<T> type, String name, boolean singleton, Provider<T> provider) {
		doDefineComponent(type, name, provider, singleton);
	}

	public <T> void defineComponent(Class<T> type, String name, Provider<T> provider) {
		doDefineComponent(type, name, provider, false);
	}

	/**
	 * Adds the given class to the list of managed classes and returns one created object as well.
	 *
	 * @param type component class
	 * @param singleton true if only one object of the given class will ever created
	 * @param provider component provider
	 * @param <T> component type
	 */
	public <T> T defineAndGetComponent(Class<T> type, boolean singleton, Provider<T> provider) {
		doDefineComponent(type, null, provider, singleton);
		T component = getComponent(type);

		return component;
	}

	public <T> T defineAndGetComponent(Class<T> type, Provider<T> provider) {
		doDefineComponent(type, null, provider, false);
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
		return getComponent(type, null);
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
	@SuppressWarnings("unchecked")
	public <T> T getComponent(Class<T> type, String name) {
		// look up component
		ComponentDef componentDef = getComponentDef(type, name);
		if (componentDef == null) {
			throw new RuntimeException("unknown component [" + type + "] named [" + name + "]");
		}

		// build component with dependencies
		// optionally return existing singleton
		if (componentDef.isSingleton()) {
			synchronized (componentDef.singletonCreationLock) {
				T singleton = (T)componentDef.getSingletonRef();
				if (singleton == null) {
					singleton = doCreateComponent(componentDef);
					componentDef.setSingletonRef(singleton);
				}
				return singleton;
			}
		} else {
			T component = doCreateComponent(componentDef);
			return component;
		}
	}

	private void doDefineComponent(Class<?> type, String name, Provider<?> provider, boolean singleton) {
		// prevent duplicates
		if (getComponentDef(type, name) != null) {
			throw new RuntimeException("duplicate component [" + type + "] named [" + name + "]");
		}

		// collect component meta data
		ComponentDef componentDef = new ComponentDef(type, name, provider);
		componentDef.setSingleton(singleton);
		List<Injection> injections = componentDef.getInjections();
		Class<?> c = type;
		while(c != null && c != Object.class) {
			// collect PostConstruct methods
			Method[] methods = c.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];
				PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
				if (postConstruct != null) {
					componentDef.setPostConstruct(method);
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

		if (name == null) {
			unnamedComponents.add(componentDef);
			unnamedComponentsByType.put(type, componentDef);
		}
		else {
			componentsByName.put(name, componentDef);
		}
	}

	private <T> void injectDependencies(T component, ComponentDef componentDef) {
		if (debug) {
			onDebug("injectDependencies for [" + component + "] [" + componentDef.getType() + "]");
		}

		// inject field values
		List<Injection> injections = componentDef.getInjections();
		int size = injections.size();
		for (int i = 0; i < size; i++) {
			Injection injection = injections.get(i);
			Field field = injection.getField();
			Class<?> injectionType = field.getType();

			// get definition for dependency
			ComponentDef injectionDef = injection.getComponentDef();
			if (injectionDef == null) {
				injectionDef = getComponentDef(injectionType, injection.getName());
				if (injectionDef == null) {
					throw new RuntimeException("no component of type [" + injectionType + "] defined for injection into [" + componentDef.getType() + "]");
				}
				injection.setComponentDef(injectionDef);
			}

			// optionally return existing singleton
			if (injectionDef.isSingleton()) {
				synchronized (injectionDef.singletonCreationLock) {
					T injectionValue = (T)injectionDef.getSingletonRef();
					if (injectionValue == null) {
						injectionValue = doCreateComponent(injectionDef);
						injectionDef.setSingletonRef(injectionValue);

						try {
							// recursively create dependencies for new components
							injectDependencies(injectionValue, injectionDef);
							field.set(component, injectionValue);
						}
						catch (Exception e) {
							throw new RuntimeException("injectDependencies", e);
						}
					}
					else {
						try {
							field.set(component, injectionValue);
						}
						catch (Exception e) {
							throw new RuntimeException("injectDependencies", e);
						}
					}
				}
			} else {
				T injectionValue = doCreateComponent(injectionDef);
				try {
					// recursively create dependencies for new components
					injectDependencies(injectionValue, injectionDef);
					field.set(component, injectionValue);
				}
				catch (Exception e) {
					throw new RuntimeException("injectDependencies", e);
				}
			}
		}
	}

	private ComponentDef getComponentDef(Class<?> type, String name) {
		// look for named component
		if (name != null) {
			ComponentDef compDef = componentsByName.get(name);
			return compDef;
		}

		// look for unnamed component
		ComponentDef compDef = unnamedComponentsByType.get(type);
		if (compDef != null) {
			return compDef;
		}

		// look for matching (unnamed) descendant component definitions
		int matchCount = 0;
		int size = unnamedComponents.size();
		for (int i = 0; i < size; i++) {
			ComponentDef tempDef = unnamedComponents.get(i);
			if (type.isAssignableFrom(tempDef.getType())) {
				compDef = tempDef;
				matchCount++;
			}
		}
		if (matchCount > 1) {
			throw new AmbigousComponentException("multiple components for injection [" + type + "] [" + name + "]");
		}

		return compDef;
	}

	private <T> T doCreateComponent(ComponentDef componentDef) {
		Provider<?> provider = componentDef.getProvider();
		T component = (T)provider.provide();
		if (component != null) {
			injectDependencies(component, componentDef);

			Method postConstruct = componentDef.getPostConstruct();
			if (postConstruct != null) {
				try {
					postConstruct.setAccessible(true);
					postConstruct.invoke(component);
				}
				catch (Exception e) {
					throw new RuntimeException("getComponent", e);
				}
			}
		}

		return component;
	}

	protected void onDebug(String msg) {
	}

	private List<ComponentDef> unnamedComponents;
	private Map<Class<?>, ComponentDef> unnamedComponentsByType;
	private Map<String, ComponentDef> componentsByName;
	private boolean debug;
}
