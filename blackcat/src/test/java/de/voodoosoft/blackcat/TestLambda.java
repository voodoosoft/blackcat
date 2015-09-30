package de.voodoosoft.blackcat;

import junit.framework.TestCase;



/**
 * Test using lambda expressions
 * @see Test
 */
public class TestLambda extends TestCase {

	/**
	 * Tests to inject one object.
	 */
	public void testSimpleInjection() {
		Injector injector = new Injector();

		injector.addComponent(OneManBand.class, OneManBand::new);
		injector.addComponent(Guitar.class, Guitar::new);

		OneManBand band = injector.getComponent(OneManBand.class);
		assertNotNull(band);
		assertNotNull(band.getGuitar());
	}

	/**
	 * Tests to inject an object which has injections by itself.
	 */
	public void testNestedInjection() {
		Injector injector = new Injector();

		injector.addComponent(Band.class, Band::new);
		injector.addComponent(Bass.class, Bass::new);
		injector.addComponent(Body.class, Body::new);

		Band band = injector.getComponent(Band.class);
		assertNotNull(band.getBass());
		assertNotNull(band.getBass().getBody());
		assertNull(band.getGuitar());
	}

	/**
	 * Tests to inject an object by type and name.
	 */
	public void testNamedInjection() {
		Injector injector = new Injector();

		injector.addComponent(Jazzband.class, Jazzband::new);
		injector.addComponent(Guitar.class, "LesPaul", () -> new Guitar("LesPaul"));
		injector.addComponent(Guitar.class, "Stratocaster", () -> new Guitar("Stratocaster"));

		Jazzband band = injector.getComponent(Jazzband.class);
		assertEquals("LesPaul", band.getLeadGuitar().getModel());
		assertEquals("Stratocaster", band.getRhythmGuitar().getModel());
		assertNotSame(band.getLeadGuitar(), band.getRhythmGuitar());

		Guitar lesPaul = injector.getComponent(Guitar.class, "LesPaul");
		assertNotNull(lesPaul);
		Guitar stratocaster = injector.getComponent(Guitar.class, "Stratocaster");
		assertNotNull(stratocaster);
		assertNotSame(lesPaul, stratocaster);
	}

	/**
	 * Tests to inject two object of the same type, one is named, the other is not.
	 */
	public void testTypedAndNamedInjection() {
		Injector injector = new Injector();

		injector.addComponent(Bluesband.class, Bluesband::new);
		injector.addComponent(Guitar.class, "LesPaul", () -> new Guitar("LesPaul"));
		injector.addComponent(Guitar.class, () -> new Guitar("Stratocaster"));

		Bluesband band = injector.getComponent(Bluesband.class);
		assertEquals("LesPaul", band.getLeadGuitar().getModel());
		assertEquals("Stratocaster", band.getRhythmGuitar().getModel());
		assertNotSame(band.getLeadGuitar(), band.getRhythmGuitar());
	}

	/**
	 * Tests to inject the same object twice.
	 */
	public void testDoubleInjection() {
		Injector injector = new Injector();
		injector.addComponent(Metalband.class, Metalband::new);
		Guitar guitar = new Guitar("Stratocaster");
		injector.addComponent(Guitar.class, () -> guitar);

		Metalband band = injector.getComponent(Metalband.class);
		assertSame(band.getLeadGuitar(), band.getRhythmGuitar());
	}

	/**
	 * Tests post construction callback.
	 */
	public void testPostConstruction() {
		Injector injector = new Injector();

		injector.addComponent(Guitar.class, Guitar::new);

		Guitar guitar = injector.getComponent(Guitar.class);
		assertNotNull(guitar);
		assertNotNull(guitar.isInitialized());
	}
}
