# Blackcat
##Lightweight dependency injection library for Java

Blackcat was developed while working on the game [Biodrone Battle](http://www.biodronebattle.com).
Requirements were as follows:
* no external dependencies
* no component scanning
* no code generation, no byte code modification, no configuration files
* no black magic, simple to use and fast

It can do two things:
* annotation based field injections
* invoke post construction callbacks

To get the gist of this library, check out the unit tests.
A first simple performance test shows that getting objects from Blackcat is faster than using Guice, Feather, PicoContainter and Spring (feel free to prove me wrong...).

Simple example for building a band consisting of guitar and bass:
```
public class Band {
	@Inject
	private Guitar guitar;

	@Inject
	private Bass bass;

	public Band() {
	}
}

public class Guitar {
	public Guitar() {
	}
}

public class Bass {
	public Bass() {
	}
}
```
```
Injector injector = Injector.getInjector();

// declare DI components
injector.defineComponent(Band.class, Band::new);
injector.defineComponent(Guitar.class, Guitar::new);
injector.defineComponent(Bass.class, Guitar::new);

// get the band together
Band band = injector.getComponent(Band.class);
```

**Basic component definition:**
```
   defineComponent(Class<T> type, Provider<T> provider) 
```
A **provider** is as simple as this:
```
public interface Provider<T> {
	T provide();
}
```
It is up to you to create, handle or store component instances.
However, there are two convenient providers you can use:
* SingletonProvider for assuring there will only be one object created for a class.
* ThreadLocalProvider always returns the same object per calling thread.

In case you need to do some post-constructor initialization, you can annotate a method with **PostConstruct**.
This is typically necessary for init code that requires all injections to be available.
```
   @PostConstruct
   private void initializeMe() {
      // all injections have been set here
   }
```	

To have multiple different components of the same class, components can be **named**.
```
public class Band {
   @Inject("stratocaster")
   private Guitar strat;

   @Inject("telecaster")
   private Guitar tele;
}

injector.defineComponent(Guitar.class, "stratocaster", Guitar::new);
injector.defineComponent(Guitar.class, "telecaster", Guitar::new);
```
