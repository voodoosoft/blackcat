package de.voodoosoft.blackcat;


public class OneManBand implements Band {
	@Inject
	private Guitar guitar;

	public OneManBand() {
	}

	public Guitar getGuitar() {
		return guitar;
	}
}
