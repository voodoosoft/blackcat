
package de.voodoosoft.blackcat;

class ProviderEntry {
	public ProviderEntry(Provider provider, Class type, String name) {
		this.provider = provider;
		this.type = type;
		this.name = name;
	}

	public Class getType() {
		return type;
	}

	public String getName() {
		return name;
	}	
	
	public Provider getProvider() {
		return provider;
	}

	private Class type;
	private String name;
	private Provider provider;
}
