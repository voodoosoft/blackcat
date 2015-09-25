package de.voodoosoft.blackcat;

public class Jazzband {
	private Bass bass;

	@Inject("Stratocaster")
	private Guitar rhythmGuitar;

	@Inject("LesPaul")
	private Guitar leadGuitar;

	public Jazzband() {
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
