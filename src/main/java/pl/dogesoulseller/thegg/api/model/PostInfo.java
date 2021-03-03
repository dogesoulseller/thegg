package pl.dogesoulseller.thegg.api.model;

import java.util.List;

import org.springframework.data.annotation.PersistenceConstructor;

/**
 * Represents user input containing new information about a post
 */
public class PostInfo {
	private String filename;
	private String rating;
	private Post parent;
	private String sourceUrl;
	private String authorComment;
	private String posterComment;
	private List<String> tags;

	@PersistenceConstructor
	public PostInfo(String filename, String rating, Post parent, String sourceUrl, String authorComment, String posterComment, List<String> tags) {
		this.filename = filename;
		this.rating = rating;
		this.parent = parent;
		this.sourceUrl = sourceUrl;
		this.authorComment = authorComment;
		this.posterComment = posterComment;
		this.tags = tags;
	}

	public String getAuthorComment() {
		return authorComment;
	}

	public String getFilename() {
		return filename;
	}

	public String getRating() {
		return rating;
	}

	public Post getParent() {
		return parent;
	}

	public String getPosterComment() {
		return posterComment;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public List<String> getTags() {
		return tags;
	}
}
