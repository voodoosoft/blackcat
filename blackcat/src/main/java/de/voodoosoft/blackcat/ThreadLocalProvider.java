package de.voodoosoft.blackcat;

/**
 * Convenient provider implementation which always returns the same class instance per thread. 
 *
 * @param <T>
 */
public class ThreadLocalProvider<T> implements Provider<T> {
	public ThreadLocalProvider(Provider<T> concreteProvider) {
		this.concreteProvider = concreteProvider;
		threadLocal = new ThreadLocal<>();
	}

	@Override
	public T provide() {
		T object = threadLocal.get();
		if (object == null) {
			object = concreteProvider.provide();
			threadLocal.set(object);
		}
		
		return object;
	}
	
	private Provider<T> concreteProvider;
	private ThreadLocal<T> threadLocal;
}