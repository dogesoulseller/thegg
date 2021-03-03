package pl.dogesoulseller.thegg.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

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

	@Indexed
	private boolean active;

	@PersistenceConstructor
	public ApiKey(String id, String key, String name, String userid, boolean active) {
		this.id = id;
		this.key = key;
		this.name = name;
		this.userid = userid;
		this.active = active;
	}

	public ApiKey(String key, String name, String userid) {
		this.key = key;
		this.name = name;
		this.userid = userid;
		this.active = true;
	}

	public String getUserID() {
		return userid;
	}

	public String getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
