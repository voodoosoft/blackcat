
package de.voodoosoft.blackcat;

import java.lang.reflect.Method;
import java.util.List;

class ComponentEntry {
	public ComponentEntry() {
	}

	public ComponentEntry(Class type, String name) {
		this.type = type;
		this.name = name;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public Class getType() {
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

	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public boolean isSingleton() {
		return singleton;
	}

	public void setSingletonHashCode(int singletonHashcode) {
		this.singletonHashcode = singletonHashcode;
	}

	public int getSingletonHashCode() {
		return singletonHashcode;
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

	private Class type;
	private String name;
	private boolean singleton;
	private int singletonHashcode;
	private List<Injection> injections;
	private Method postConstruct;
	private final Object lock = new Object();
}
