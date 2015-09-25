
package de.voodoosoft.blackcat;

class Injection {
	public Injection(Class holderType, Class type, String field, String name) {
		this.holderType = holderType;
		this.type = type;
		this.field = field;
		this.name = name;
	}

	public Class getType() {
		return type;
	}

	public String getField() {
		return field;
	}

	public String getName() {
		return name;
	}

	public Class getHolderType() {
		return holderType;
	}

	private Class holderType;
	private Class type;
	private String field;
	private String name;
}
