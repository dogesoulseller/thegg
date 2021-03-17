package pl.dogesoulseller.thegg.api.model;

import java.util.List;

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

	public PostInfo(String filename, String rating, Post parent, String sourceUrl, String authorComment, String posterComment,
	                List<String> tags) {
		this.filename = filename;
		this.rating = rating;
		this.parent = parent;
		this.sourceUrl = sourceUrl;
		this.authorComment = authorComment;
		this.posterComment = posterComment;
		this.tags = tags;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getRating() {
		return this.rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public Post getParent() {
		return this.parent;
	}

	public void setParent(Post parent) {
		this.parent = parent;
	}

	public String getSourceUrl() {
		return this.sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getAuthorComment() {
		return this.authorComment;
	}

	public void setAuthorComment(String authorComment) {
		this.authorComment = authorComment;
	}

	public String getPosterComment() {
		return this.posterComment;
	}

	public void setPosterComment(String posterComment) {
		this.posterComment = posterComment;
	}

	public List<String> getTags() {
		return this.tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
