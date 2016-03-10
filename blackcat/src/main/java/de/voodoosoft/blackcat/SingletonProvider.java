package de.voodoosoft.blackcat;

/**
 * Convenient provider implementation which always returns the same class instance. 
 *
 * @param <T>
 */
public class SingletonProvider<T> implements Provider<T> {
	public SingletonProvider(Provider<T> concreteProvider) {
		this.concreteProvider = concreteProvider;
	}

	@Override
	public synchronized T provide() {
		if (singleton == null) {
			singleton = concreteProvider.provide();
		}
		
		return singleton;
	}
	
	private T singleton;
	private Provider<T> concreteProvider;
}