package pl.dogesoulseller.thegg.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Data about a single gallery post
 */
@Document(collection = "posts")
public class Post {
	@Transient
	private static final String[] VALID_RATINGS = {"safe", "questionable", "explicit", "violent"};

	@Transient
	private static final Pattern TAG_REPLACEMENT = Pattern.compile("\\s+|[!@#$%^&*()_+\\-=~`{}\\[\\]:;'\",.<>/?\\\\|]");

	@Id
	private String id;

	@Indexed
	@DBRef
	private Post parent;

	@Indexed
	private String poster;

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

	/**
	 * Constructor building a post from received {@link NewPostInfo}
	 *
	 * @param info info received from API request
	 * @throws RuntimeException on invalid rating
	 */
	public Post(NewPostInfo info) throws RuntimeException {
		this.authorComment = info.getAuthorComment();
		this.filename = info.getFilename();
		this.rating = info.getRating().strip().toLowerCase();
		this.parent = info.getParent();
		this.posterComment = info.getPosterComment();
		this.sourceUrl = info.getSourceUrl();
		this.creationDate = Instant.now();

		// Sanitize tags
		this.tags = new ArrayList<>(info.getTags().size());
		for (var tag : info.getTags()) {
			this.tags.add(TAG_REPLACEMENT.matcher(tag.toLowerCase()).replaceAll("_"));
		}

		// Validate rating
		for (var r : VALID_RATINGS) {
			if (rating.equals(r)) {
				return;
			}
		}

		throw new RuntimeException("Rating invalid");
	}

	@PersistenceConstructor
	public Post(String id, Post parent, String poster, String sourceUrl, String filename, String rating,
				Instant creationDate, Instant modificationDate, long filesize, String mime, int width, int height, String authorComment,
				String posterComment, List<String> tags, boolean deleted, String deletionReason) {
		this.id = id;
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

	public void update(NewPostInfo info) {
		this.authorComment = info.getAuthorComment() == null ? this.authorComment : info.getAuthorComment();
		this.posterComment = info.getPosterComment() == null ? this.posterComment : info.getPosterComment();
		this.parent = info.getParent() == null ? this.parent : info.getParent();
		this.tags = info.getTags() == null ? this.tags : info.getTags();
		this.sourceUrl = info.getSourceUrl() == null ? this.sourceUrl : info.getSourceUrl();
		this.rating = info.getRating() == null ? this.rating : info.getRating();

		setSanitizedTags(info.getTags());

		// Validate rating
		for (var r : VALID_RATINGS) {
			if (rating.equals(r)) {
				return;
			}
		}

		throw new RuntimeException("Rating invalid");
	}

	private void setSanitizedTags(List<String> tags) {
		if (tags != null) {
			this.tags = new ArrayList<>(tags.size());
			for (var tag : tags) {
				this.tags.add(TAG_REPLACEMENT.matcher(tag.toLowerCase()).replaceAll("_"));
			}
		}
	}

	public void updateFull(NewPostInfo info) {
		this.authorComment = info.getAuthorComment();
		this.posterComment = info.getPosterComment();
		this.parent = info.getParent();
		this.tags = info.getTags();
		this.sourceUrl = info.getSourceUrl();
		this.rating = info.getRating();

		setSanitizedTags(info.getTags());

		// Validate rating
		for (var r : VALID_RATINGS) {
			if (rating.equals(r)) {
				return;
			}
		}

		throw new RuntimeException("Rating invalid");
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Post getParent() {
		return this.parent;
	}

	public void setParent(Post parent) {
		this.parent = parent;
	}

	public String getPoster() {
		return this.poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getSourceUrl() {
		return this.sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
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

	public Instant getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(Instant creationDate) {
		this.creationDate = creationDate;
	}

	public Instant getModificationDate() {
		return this.modificationDate;
	}

	public void setModificationDate(Instant modificationDate) {
		this.modificationDate = modificationDate;
	}

	public long getFilesize() {
		return this.filesize;
	}

	public void setFilesize(long filesize) {
		this.filesize = filesize;
	}

	public String getMime() {
		return this.mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}

	public int getWidth() {
		return this.width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(int height) {
		this.height = height;
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

	public boolean isDeleted() {
		return this.deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getDeletionReason() {
		return this.deletionReason;
	}

	public void setDeletionReason(String deletionReason) {
		this.deletionReason = deletionReason;
	}
}
