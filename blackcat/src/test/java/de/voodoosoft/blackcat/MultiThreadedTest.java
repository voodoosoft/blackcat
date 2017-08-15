package de.voodoosoft.blackcat;

import org.junit.Assert;



public class MultiThreadedTest {
	static Concert concert1;
	static Concert concert2;

	public static void main(String[] args) {

		Injector injector = Injector.getInjector();
		injector.defineComponent(Band.class, true, () -> new Jazzband());
		injector.defineComponent(Concert.class, true, () -> new Concert());

		Runnable r1 = new Runnable() {
			@Override
			public void run() {
				System.out.println("r1 start");
				concert1 = injector.getComponent(Concert.class);
				System.out.println("r1: " + concert1);
			}
		};
		Thread t1 = new Thread(r1);

		Runnable r2 = new Runnable() {
			@Override
			public void run() {
				System.out.println("r2 start");
				concert2 = injector.getComponent(Concert.class);
				System.out.println("r2: " + concert2);
			}
		};
		Thread t2 = new Thread(r2);

		t1.start();
		t2.start();

		try {
			t1.join();
			t2.join();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		Assert.assertEquals(concert1, concert2);
	}
}
