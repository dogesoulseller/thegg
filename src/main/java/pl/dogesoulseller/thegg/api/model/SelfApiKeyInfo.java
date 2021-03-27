package pl.dogesoulseller.thegg.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class SelfApiKeyInfo {
	private String name;
	private Instant creationtime;
	private Boolean active;

	@JsonCreator
	public SelfApiKeyInfo(@JsonProperty("name") String name, @JsonProperty("creationtime") Instant creationtime, @JsonProperty("active") Boolean active) {
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
