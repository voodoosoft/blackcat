package de.voodoosoft.blackcat;

public class Artist {
	public Artist() {
	}

	public Artist(String name) {
		this.name = name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	private String name;
}
