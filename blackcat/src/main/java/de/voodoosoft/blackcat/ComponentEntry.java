
package de.voodoosoft.blackcat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal {@link Injector} class for holding defined components. 
 */
class ComponentEntry {
	public ComponentEntry() {
		injections = new ArrayList<>();
	}

	public ComponentEntry(Class<?> type, String name, Provider provider) {
		this.type = type;
		this.name = name;
		this.provider = provider;
		injections = new ArrayList<>();
	}

	public Provider<?> getProvider() {
		return provider;
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public Class<?> getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setInjections(List<Injection> injections) {
		this.injections = injections;
	}
	
	public List<Injection> getInjections() {
		return injections;
	}

	public void setPostConstruct(Method postConstruct) {
		this.postConstruct = postConstruct;
	}

	public Method getPostConstruct() {
		return postConstruct;
	}

	public Object getLock() {
		return lock;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ComponentEntry componentEntry = (ComponentEntry)o;

		if (!type.equals(componentEntry.type))
			return false;
		return !(name != null ? !name.equals(componentEntry.name) : componentEntry.name != null);

	}

	@Override
	public int hashCode() {
		int result = type.hashCode();
		result = 31 * result + (name != null ? name.hashCode() : 0);
		return result;
	}

	private Class<?> type;
	private String name;
	private List<Injection> injections;
	private Method postConstruct;
	private final Object lock = new Object();
	private Provider<?> provider;
}
