package pl.dogesoulseller.thegg.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a tag's information
 */
@Document(collection = "tags")
public class Tag {
	@Id
	private String id;

	@Indexed
	private String tag;

	private String description;

	@PersistenceConstructor
	public Tag(String id, String tag, String description) {
		this.id = id;
		this.tag = tag;
		this.description = description;
	}

	/**
	 * Construct new tag with empty description
	 * @param tag tag name
	 */
	public Tag(String tag) {
		this.tag = tag;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getTag() {
		return tag;
	}
}
