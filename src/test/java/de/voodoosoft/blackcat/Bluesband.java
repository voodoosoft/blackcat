package de.voodoosoft.blackcat;


public class Bluesband {
	private Bass bass;

	@Inject
	private Guitar rhythmGuitar;

	@Inject("LesPaul")
	private Guitar leadGuitar;

	public Bluesband() {
	}

	public Guitar getRhythmGuitar() {
		return rhythmGuitar;
	}

	public Guitar getLeadGuitar() {
		return leadGuitar;
	}

	public Bass getBass() {
		return bass;
	}
}
