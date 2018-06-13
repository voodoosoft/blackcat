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

		injector.defineComponent(Bass.class, Bass::new);
		injector.defineComponent(Body.class, Body::new);

		Bass bass = injector.getComponent(Bass.class);
		assertNotNull(bass);
		assertNotNull(bass.getBody());
	}

	/**
	 * Tests to inject an object which has injections by itself.
	 */
	public void testNestedInjection() {
		Injector injector = new Injector();

		injector.defineComponent(StringBand.class, StringBand::new);
		injector.defineComponent(Bass.class, Bass::new);
		injector.defineComponent(Body.class, Body::new);

		StringBand band = injector.getComponent(StringBand.class);
		assertNotNull(band.getBass());
		assertNotNull(band.getBass().getBody());
	}

	/**
	 * Tests to inject an object by type and name.
	 */
	public void testNamedInjection() {
		Injector injector = new Injector();

		injector.defineComponent(Jazzband.class, Jazzband::new);
		injector.defineComponent(Guitar.class, "LesPaul", () -> new Guitar("LesPaul"));
		injector.defineComponent(Guitar.class, "Stratocaster", () -> new Guitar("Stratocaster"));
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);

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

		injector.defineComponent(Bluesband.class, Bluesband::new);
		injector.defineComponent(Guitar.class, "LesPaul", () -> new Guitar("LesPaul"));
		injector.defineComponent(Guitar.class, () -> new Guitar("Stratocaster"));
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);

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
		
		injector.defineComponent(Metalband.class, Metalband::new);
		Guitar guitar = new Guitar("Stratocaster");
		injector.defineComponent(Guitar.class, () -> guitar);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);

		Metalband band = injector.getComponent(Metalband.class);
		assertSame(band.getLeadGuitar(), band.getRhythmGuitar());
	}

	/**
	 * Tests post construction callback.
	 */
	public void testPostConstruction() {
		Injector injector = new Injector();

		injector.defineComponent(Guitar.class, Guitar::new);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);

		Guitar guitar = injector.getComponent(Guitar.class);
		assertNotNull(guitar);
		assertNotNull(guitar.isInitialized());
	}

	/**
	 * Tests post construction callback.
	 */
	public void testMultipleCreation() {
		Injector injector = new Injector();

		injector.defineComponent(Guitar.class, Guitar::new);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);

		Guitar guitar = injector.getComponent(Guitar.class);
		assertNotNull(guitar);
		assertNotNull(guitar.isInitialized());

		Guitar guitar2 = injector.getComponent(Guitar.class);
		assertNotNull(guitar2);
		assertNotNull(guitar2.isInitialized());

		Guitar guitar3 = injector.getComponent(Guitar.class);
		assertNotNull(guitar3);
		assertNotNull(guitar3.isInitialized());

		assertTrue(guitar != guitar2);
		assertTrue(guitar2 != guitar3);
	}
	
	public void testSingleton() {
		Injector injector = new Injector();
		injector.defineComponent(Bass.class, true, (() -> new Bass()));
		injector.defineComponent(Body.class, () -> new Body());
		
		Bass bass1 = injector.getComponent(Bass.class);
		Bass bass2 = injector.getComponent(Bass.class);
		assertTrue(bass1 == bass2);
	}
	
	public void testDistinctness() {
		Injector injector = new Injector();
		injector.defineComponent(Bass.class,() -> new Bass());
		injector.defineComponent(Body.class, () -> new Body());
		
		Bass bass1 = injector.getComponent(Bass.class);
		Bass bass2 = injector.getComponent(Bass.class);
		assertTrue(bass1 != bass2);
	}

	public void testAncestor() {
		Injector injector = new Injector();
		
		// component with interface injection of type "Band"
		injector.defineComponent(Concert.class, Concert::new);
		
		// concrete class "Metalband" to serve for all injections of type "Band"
		injector.defineComponent(Metalband.class, Metalband::new);
		
		injector.defineComponent(Guitar.class, Guitar::new);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);
		
		Concert concert = injector.getComponent(Concert.class);
		assertNotNull(concert);
		Metalband band = (Metalband) concert.getBand();
		assertNotNull(band);
		assertNotNull(band.getLeadGuitar());
		assertNotNull(band.getRhythmGuitar());
	}
	
	public void testAmbigousInjection() {
		Injector injector = new Injector();
		
		injector.defineComponent(BrokenBand.class, BrokenBand::new);
		injector.defineComponent(Guitar.class, Guitar::new);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);
		injector.defineComponent(Drums.class, Drums::new);
		
		// instrument cannot be injected because it is ambigous
		BrokenBand band = null;
		try {
			band = injector.getComponent(BrokenBand.class);
		} catch (AmbigousComponentException ex) {
		}
		assertNull(band);
	}

	public void testInheritedComponents() {
		Injector injector = new Injector();

		injector.defineComponent(ElectricGuitar.class, ElectricGuitar::new);
		injector.defineComponent(Guitar.class, "Dreadnought", () -> new Guitar("Dreadnought"));
		injector.defineComponent(Guitar.class, "LesPaul", () -> new Guitar("LesPaul"));
		injector.defineComponent(ElectricGuitar.class, "Stratocaster", () -> new ElectricGuitar("Stratocaster"));
		injector.defineComponent(Guitar.class, Guitar::new);
		injector.defineComponent(GuitarCollection.class, GuitarCollection::new);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);

		GuitarCollection collection = injector.getComponent(GuitarCollection.class);
		assertNotNull(collection.getGuitar1());
		assertTrue(collection.getGuitar1() instanceof Guitar);
		assertNotNull(collection.getGuitar2());
		assertTrue(collection.getGuitar2() instanceof ElectricGuitar);
		assertNotNull(collection.getGuitar3());
		assertTrue(collection.getGuitar3() instanceof ElectricGuitar);
		assertTrue(collection.getGuitar3().getModel().equals("Stratocaster"));
		assertNotNull(collection.getGuitar4());
		assertTrue(collection.getGuitar4() instanceof Guitar);
		assertTrue(collection.getGuitar4().getModel().equals("Dreadnought"));

	}
}
