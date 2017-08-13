package de.voodoosoft.blackcat;

public class Concert {
	public Concert() {
	}

	public Band getBand() {
		return band;
	}

	@Inject
	private Band band;
}
