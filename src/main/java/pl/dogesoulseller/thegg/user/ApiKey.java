package pl.dogesoulseller.thegg.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Information about an API key
 */
@Document(collection = "apikeys")
public class ApiKey {
	@Id
	private String id;

	@Indexed
	private String key;

	private String name;

	@Indexed
	private String userid;

	private Instant creationtime;

	@Indexed
	private boolean active;

	@Indexed
	private boolean adminKey;

	@PersistenceConstructor
	public ApiKey(String id, String key, String name, String userid, Instant creationtime, boolean active, boolean adminKey) {
		this.id = id;
		this.key = key;
		this.name = name;
		this.userid = userid;
		this.creationtime = creationtime;
		this.active = active;
		this.adminKey = adminKey;
	}

	public ApiKey(String key, String name, String userid) {
		this.key = key;
		this.name = name;
		this.userid = userid;
		this.active = true;
		this.creationtime = Instant.now();
	}

	public String getUserID() {
		return userid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public Instant getCreationtime() {
		return creationtime;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isAdminKey() {
		return adminKey;
	}

	public void setAdminKey(boolean adminKey) {
		this.adminKey = adminKey;
	}
}
