package pl.dogesoulseller.thegg.api.model;

import java.time.Instant;

import lombok.*;

@AllArgsConstructor
@Getter
public class SelfApiKeyInfo {
	private String name;
	private Instant creationtime;
	private Boolean active;
}
