
package de.voodoosoft.blackcat;

/**
 * Supplies injected dependencies for one type.
 *
 * @param <T> supplied dependency type
 */
public interface Provider<T> {
	T provide();
}
