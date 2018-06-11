package de.voodoosoft.blackcat;


public class GuitarCollection {
	@Inject
	private Guitar guitar1;

	@Inject
	private ElectricGuitar guitar2;

	@Inject("Stratocaster")
	private ElectricGuitar guitar3;

	@Inject("Dreadnought")
	private Guitar guitar4;

	public GuitarCollection() {
	}

	public Guitar getGuitar1() {
		return guitar1;
	}

	public ElectricGuitar getGuitar2() {
		return guitar2;
	}

	public Guitar getGuitar3() {
		return guitar3;
	}

	public Guitar getGuitar4() {
		return guitar4;
	}
}
