package pl.dogesoulseller.thegg.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

/**
 * Represents a tag's information
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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
}
