
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

	private Field field;
	private String name;
}
