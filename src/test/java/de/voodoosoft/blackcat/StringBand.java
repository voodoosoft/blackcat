package de.voodoosoft.blackcat;


public class StringBand implements Band {
	private Guitar guitar;

	@Inject
	private Bass bass;

	public StringBand() {
	}

	public Guitar getGuitar() {
		return guitar;
	}

	public Bass getBass() {
		return bass;
	}
}
