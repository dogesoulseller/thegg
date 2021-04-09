package pl.dogesoulseller.thegg.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Info about a new API key
 */
public class SelfApiKeyInfo {
	private final String name;
	private final Instant creationtime;
	private final Boolean active;

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
