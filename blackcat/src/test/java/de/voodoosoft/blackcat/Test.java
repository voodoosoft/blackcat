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

		injector.defineComponent(OneManBand.class, new Provider<OneManBand>() {
			@Override
			public OneManBand provide() {
				return new OneManBand();
			}
		});
		injector.defineComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar();
			}
		});
		injector.defineComponent(Artist.class, new Provider<Artist>() {
			@Override
			public Artist provide() {
				return new Artist();
			}
		});
		injector.defineComponent(Body.class, new Provider<Body>() {
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

		injector.defineComponent(StringBand.class, new Provider<StringBand>() {
			@Override
			public StringBand provide() {
				return new StringBand();
			}
		});
		injector.defineComponent(Bass.class, new Provider<Bass>() {
			@Override
			public Bass provide() {
				return new Bass();
			}
		});
		injector.defineComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

		StringBand band = injector.getComponent(StringBand.class);
		assertNotNull(band.getBass());
		assertNotNull(band.getBass().getBody());
		assertNull(band.getGuitar());
	}

	/**
	 * Tests to inject an object by type and name.
	 */
	public void testNamedInjection() {
		Injector injector = new Injector();

		injector.defineComponent(Jazzband.class, new Provider<Jazzband>() {
			@Override
			public Jazzband provide() {
				return new Jazzband();
			}
		});
		injector.defineComponent(Guitar.class, "LesPaul", new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("LesPaul");
			}
		});
		injector.defineComponent(Guitar.class, "Stratocaster", new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("Stratocaster");
			}
		});
		injector.defineComponent(Artist.class, new Provider<Artist>() {
			@Override
			public Artist provide() {
				return new Artist();
			}
		});
		injector.defineComponent(Body.class, new Provider<Body>() {
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

		injector.defineComponent(Bluesband.class, new Provider<Bluesband>() {
			@Override
			public Bluesband provide() {
				return new Bluesband();
			}
		});
		injector.defineComponent(Guitar.class, "LesPaul", new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("LesPaul");
			}
		});
		injector.defineComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar("Stratocaster");
			}
		});
		injector.defineComponent(Artist.class, new Provider<Artist>() {
			@Override
			public Artist provide() {
				return new Artist();
			}
		});
		injector.defineComponent(Body.class, new Provider<Body>() {
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

		injector.defineComponent(Metalband.class, new Provider<Metalband>() {
			@Override
			public Metalband provide() {
				return new Metalband();
			}
		});
		injector.defineComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return guitar;
			}
		});
		injector.defineComponent(Artist.class, new Provider<Artist>() {
			@Override
			public Artist provide() {
				return new Artist();
			}
		});
		injector.defineComponent(Body.class, new Provider<Body>() {
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

		injector.defineComponent(Guitar.class, new Provider<Guitar>() {
			@Override
			public Guitar provide() {
				return new Guitar();
			}
		});
		injector.defineComponent(Artist.class, new Provider<Artist>() {
			@Override
			public Artist provide() {
				return new Artist();
			}
		});
		injector.defineComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});

		Guitar guitar = injector.getComponent(Guitar.class);
		assertNotNull(guitar);
		assertNotNull(guitar.isInitialized());
	}
	
	public void testSingleton() {
		Injector injector = new Injector();
		injector.defineComponent(Bass.class, new SingletonProvider<>(
				new Provider<Bass>() {
					@Override
					public Bass provide() {
						return new Bass();
					}
				}
		));
		injector.defineComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});
		
		Bass bass1 = injector.getComponent(Bass.class);
		Bass bass2 = injector.getComponent(Bass.class);
		assertTrue(bass1 == bass2);
	}
	
	public void testDistinctness() {
		Injector injector = new Injector();
		injector.defineComponent(Bass.class, new Provider<Bass>() {
				@Override
				public Bass provide() {
					return new Bass();
				}
			}
		);
		injector.defineComponent(Body.class, new Provider<Body>() {
			@Override
			public Body provide() {
				return new Body();
			}
		});
		
		Bass bass1 = injector.getComponent(Bass.class);
		Bass bass2 = injector.getComponent(Bass.class);
		assertTrue(bass1 != bass2);
	}
	
	public void testThreadLocal() throws InterruptedException {
		Injector injector = new Injector();
		injector.defineComponent(Bass.class, new ThreadLocalProvider<Bass>(() -> new Bass()));
		injector.defineComponent(Body.class, () -> new Body());
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				bass1a = injector.getComponent(Bass.class);
				bass1b = injector.getComponent(Bass.class);
			}
		});

		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				bass2a = injector.getComponent(Bass.class);
				bass2b = injector.getComponent(Bass.class);
			}
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
	
	private Bass bass1a;
	private Bass bass1b;
	private Bass bass2a;
	private Bass bass2b;
}
