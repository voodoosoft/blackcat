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

		injector.defineComponent(OneManBand.class, OneManBand::new);
		injector.defineComponent(Guitar.class, Guitar::new);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Artist.class, Artist::new);

		OneManBand band = injector.getComponent(OneManBand.class);
		assertNotNull(band);
		assertNotNull(band.getGuitar());
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
		injector.defineComponent(Bass.class, new SingletonProvider<>(() -> new Bass()));
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
	
	public void testThreadLocal() throws InterruptedException {
		Injector injector = new Injector();
		injector.defineComponent(Bass.class, new ThreadLocalProvider<>(() -> new Bass()));
		injector.defineComponent(Body.class, () -> new Body());
		
		Thread t1 = new Thread(() -> {
			bass1a = injector.getComponent(Bass.class);
			bass1b = injector.getComponent(Bass.class);
		});

		Thread t2 = new Thread(() -> {
			bass2a = injector.getComponent(Bass.class);
			bass2b = injector.getComponent(Bass.class);
		});
		
		t1.start();
		t2.start();
		t1.join();
		t2.join();
		
		assertTrue(bass1a != bass2a);
		assertTrue(bass1a != bass2b);
		assertTrue(bass1b != bass2b);
		assertTrue(bass1b != bass2a);
		assertTrue(bass1a == bass1b);
		assertTrue(bass2a == bass2b);
	}

	public void testAncestor() {
		Injector injector = new Injector();
		injector.defineComponent(Band.class, Metalband::new); // register interface
		injector.defineComponent(Guitar.class, Guitar::new);
		injector.defineComponent(Body.class, Body::new);
		injector.defineComponent(Concert.class, Concert::new);

		Concert concert = injector.getComponent(Concert.class);
		assertNotNull(concert);
		assertNotNull(concert.getBand());
	}

	private Bass bass1a;
	private Bass bass1b;
	private Bass bass2a;
	private Bass bass2b;
}
