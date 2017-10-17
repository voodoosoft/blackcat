package de.voodoosoft.blackcat;

public abstract class Instrument {
	@Inject
	private Artist owner;
	
	public Artist getOwner() {
		return owner;
	}
}
