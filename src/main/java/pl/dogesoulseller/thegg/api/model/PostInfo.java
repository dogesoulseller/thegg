package pl.dogesoulseller.thegg.api.model;

import java.util.List;

import lombok.*;

/**
 * Represents user input containing new information about a post
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostInfo {
	private String filename;
	private String rating;
	private Post parent;
	private String sourceUrl;
	private String authorComment;
	private String posterComment;
	private List<String> tags;
}
