package de.voodoosoft.blackcat;

import junit.framework.TestCase;



/**
 * Tests using anonymus classes.
 * @see TestLambda
 */
public class Test extends TestCase {

	/**
	 * Tests to inject one object.
	 */
	public void testSimpleInjection() {
		Injector injector = new Injector();

		injector.addComponent(OneManBand.class, new Provider<OneManBand>() {
			@Override
			public OneManBand provide() {
				return new OneManBand();
			}
		});
		injector.addComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar();
			}
		});
		injector.addComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

		OneManBand band = injector.getComponent(OneManBand.class);
		assertNotNull(band);
		assertNotNull(band.getGuitar());
	}

	/**
	 * Tests to inject an object which has injections by itself.
	 */
	public void testNestedInjection() {
		Injector injector = new Injector();

		injector.addComponent(Band.class, new Provider<Band>() {
			@Override
			public Band provide() {
				return new Band();
			}
		});
		injector.addComponent(Bass.class, new Provider<Bass>() {
			@Override
			public Bass provide() {
				return new Bass();
			}
		});
		injector.addComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

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

		injector.addComponent(Jazzband.class, new Provider<Jazzband>() {
			@Override
			public Jazzband provide() {
				return new Jazzband();
			}
		});
		injector.addComponent(Guitar.class, "LesPaul", new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("LesPaul");
			}
		});
		injector.addComponent(Guitar.class, "Stratocaster", new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("Stratocaster");
			}
		});
		injector.addComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

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

		injector.addComponent(Bluesband.class, new Provider<Bluesband>() {
			@Override
			public Bluesband provide() {
				return new Bluesband();
			}
		});
		injector.addComponent(Guitar.class, "LesPaul", new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("LesPaul");
			}
		});
		injector.addComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("Stratocaster");
			}
		});
		injector.addComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

		Bluesband band = injector.getComponent(Bluesband.class);
		assertEquals("LesPaul", band.getLeadGuitar().getModel());
		assertEquals("Stratocaster", band.getRhythmGuitar().getModel());
		assertNotSame(band.getLeadGuitar(), band.getRhythmGuitar());
	}

	/**
	 * Tests to inject the same object twice.
	 */
	public void testDoubleInjection() {
		final Guitar guitar = new Guitar("Stratocaster");
		Injector injector = new Injector();

		injector.addComponent(Metalband.class, new Provider<Metalband>() {
			@Override
			public Metalband provide() {
				return new Metalband();
			}
		});
		injector.addComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return guitar;
			}
		});
		injector.addComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

		Metalband band = injector.getComponent(Metalband.class);
		assertSame(band.getLeadGuitar(), band.getRhythmGuitar());
	}

	/**
	 * Tests post construction callback.
	 */
	public void testPostConstruction() {
		Injector injector = new Injector();

		injector.addComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar();
			}
		});
		injector.addComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

		Guitar guitar = injector.getComponent(Guitar.class);
		assertNotNull(guitar);
		assertNotNull(guitar.isInitialized());
	}
}
