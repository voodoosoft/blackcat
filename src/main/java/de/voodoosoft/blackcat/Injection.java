
package de.voodoosoft.blackcat;

import java.lang.reflect.Field;

/**
 * Internal class for holding injection points.
 */
class Injection {
	public Injection(Field field, String name) {
		this.field = field;
		this.name = name;
	}

	public Field getField() {
		return field;
	}

	public String getName() {
		return name;
	}

	public void setComponentDef(ComponentDef componentDef) {
		this.componentDef = componentDef;
	}
	
	public ComponentDef getComponentDef() {
		return componentDef;
	}
	
	private Field field;
	private String name;
	private ComponentDef componentDef;
}
