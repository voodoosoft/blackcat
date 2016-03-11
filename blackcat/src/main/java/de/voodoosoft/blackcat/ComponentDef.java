
package de.voodoosoft.blackcat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal {@link Injector} class for holding defined components. 
 */
class ComponentDef {
	public ComponentDef(Class<?> type, String name, Provider<?> provider) {
		this.type = type;
		this.name = name;
		this.provider = provider;
		injections = new ArrayList<>();
	}

	public Provider<?> getProvider() {
		return provider;
	}

	public Class<?> getType() {
		return type;
	}

	public String getName() {
		return name;
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

	private Class<?> type;
	private String name;
	private List<Injection> injections;
	private Method postConstruct;
	private Provider<?> provider;
}
