package de.voodoosoft.blackcat;


public class Band {
	private Guitar guitar;

	@Inject
	private Bass bass;

	public Band() {
	}

	public Guitar getGuitar() {
		return guitar;
	}

	public Bass getBass() {
		return bass;
	}
}
