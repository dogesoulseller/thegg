package pl.dogesoulseller.thegg.api.model;

import java.time.Instant;

public class SelfApiKeyInfo {
	private String name;
	private Instant creationtime;
	private Boolean active;

	public SelfApiKeyInfo(String name, Instant creationtime, Boolean active) {
		this.name = name;
		this.creationtime = creationtime;
		this.active = active;
	}

	public String getName() {
		return this.name;
	}

	public Instant getCreationtime() {
		return this.creationtime;
	}

	public Boolean getActive() {
		return this.active;
	}
}
