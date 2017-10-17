package de.voodoosoft.blackcat;


public class Metalband implements Band {
	@Inject
	private Guitar rhythmGuitar;

	@Inject
	private Guitar leadGuitar;

	public Metalband() {
	}

	public Guitar getRhythmGuitar() {
		return rhythmGuitar;
	}

	public Guitar getLeadGuitar() {
		return leadGuitar;
	}
}
