package pl.dogesoulseller.thegg.api.model;

import java.time.Instant;

public class SelfApiKeyInfo {
	private Instant creationtime;
	private String name;
	private Boolean active;

	public SelfApiKeyInfo(String name, Instant creationtime, Boolean active) {
		this.creationtime = creationtime;
		this.name = name;
		this.active = active;
	}

	public Boolean getActive() {
		return active;
	}

	public Instant getCreationtime() {
		return creationtime;
	}

	public String getName() {
		return name;
	}
}
