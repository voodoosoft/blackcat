package de.voodoosoft.blackcat;

public class Guitar extends Instrument {
	private String model;
	private boolean initialized;

	@Inject
	private Body body;

	@PostConstruct
	private void initialize() {
		initialized = true;
	}

	public Guitar() {
	}

	public Guitar(String model) {
		this.model = model;
	}

	public String getModel() {
		return model;
	}

	public boolean isInitialized() {
		return initialized;
	}
}
