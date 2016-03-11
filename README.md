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
