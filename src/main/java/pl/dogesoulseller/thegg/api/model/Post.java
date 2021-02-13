package pl.dogesoulseller.thegg.api.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import pl.dogesoulseller.thegg.user.User;

@Document(collection = "posts")
public class Post {
	@Id
	private String Id;

	@Indexed
	@DBRef
	private Post parent;

	@Indexed
	@DBRef
	private User poster;

	@Field("source_url")
	private String sourceUrl;

	private String filename;

	@Indexed
	private String rating;

	@Field("creation_date")
	private Instant creationDate;

	@Field("modification_date")
	private Instant modificationDate;

	@Field("size")
	private long filesize;

	@Indexed
	private String mime;

	private int width;

	private int height;

	@Field("author_comment")
	private String authorComment;

	@Field("poster_comment")
	private String posterComment;

	@Indexed
	private List<String> tags;

	@Indexed
	private boolean deleted;

	private String deletionReason;

	@PersistenceConstructor
	public Post(String Id, Post parent, User poster, String sourceUrl, String filename, String rating,
			Instant creationDate, Instant modificationDate, long filesize, String mime, int width, int height,
			String authorComment, String posterComment, List<String> tags, Boolean deleted, String deletionReason) {
		this.Id = Id;
		this.parent = parent;
		this.poster = poster;
		this.sourceUrl = sourceUrl;
		this.filename = filename;
		this.rating = rating;
		this.creationDate = creationDate;
		this.modificationDate = modificationDate;
		this.filesize = filesize;
		this.mime = mime;
		this.width = width;
		this.height = height;
		this.authorComment = authorComment;
		this.posterComment = posterComment;
		this.tags = tags;
		this.deleted = deleted;
		this.deletionReason = deletionReason;
	}

	public Post() {
	}

	public Post(PostInfo info) {
		this.authorComment = info.getAuthorComment();
		this.filename = info.getFilename();
		this.rating = info.getRating();
		this.parent = info.getParent();
		this.posterComment = info.getPosterComment();
		this.sourceUrl = info.getSourceUrl();
		this.tags = info.getTags();

		this.creationDate = Instant.now();
	}

	public String getAuthorComment() {
		return authorComment;
	}

	public Instant getCreationDate() {
		return creationDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public String getDeletionReason() {
		return deletionReason;
	}

	public String getFilename() {
		return filename;
	}

	public String getRating() {
		return rating;
	}

	public Long getFilesize() {
		return filesize;
	}

	public Integer getHeight() {
		return height;
	}

	public Integer getWidth() {
		return width;
	}

	public String getMime() {
		return mime;
	}

	public Instant getModificationDate() {
		return modificationDate;
	}

	public Post getParent() {
		return parent;
	}

	public User getPoster() {
		return poster;
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

	public String getId() {
		return Id;
	}

	public void setAuthorComment(String authorComment) {
		this.authorComment = authorComment;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void setDeletionReason(String deletionReason) {
		this.deletionReason = deletionReason;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public void setModificationDate(Instant modificationDate) {
		this.modificationDate = modificationDate;
	}

	public void setParent(Post parent) {
		this.parent = parent;
	}

	public void setPoster(User poster) {
		this.poster = poster;
	}

	public void setPosterComment(String posterComment) {
		this.posterComment = posterComment;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
}
