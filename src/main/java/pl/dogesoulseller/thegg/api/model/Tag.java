package pl.dogesoulseller.thegg.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

	/**
	 * Construct new tag with empty description
	 * @param tag tag name
	 */
	public Tag(String tag) {
		this.tag = tag;
	}

	@JsonCreator
	@PersistenceConstructor
	public Tag(@JsonProperty("id") String id, @JsonProperty("tag") String tag, @JsonProperty("description") String description) {
		this.id = id;
		this.tag = tag;
		this.description = description;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTag() {
		return this.tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
