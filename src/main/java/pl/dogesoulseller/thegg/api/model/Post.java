package pl.dogesoulseller.thegg.api.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;

/**
 * Data about a single gallery post
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "posts")
public class Post {
	@Transient
	private static final String[] VALID_RATINGS = {"safe", "questionable", "explicit", "violent"};

	@Transient
	private static final Pattern TAG_REPLACEMENT = Pattern.compile("\\s+|[!@#$%^&*()_+\\-=~`{}\\[\\]:;'\",.<>\\/?\\\\|]");

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
	 * Constructor building a post from received {@link PostInfo}
	 * @param info info received from API request
	 * @throws RuntimeException on invalid rating
	 */
	public Post(PostInfo info) throws RuntimeException {
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
			tag = TAG_REPLACEMENT.matcher(tag.toLowerCase()).replaceAll("_");
			this.tags.add(tag);
		}

		// Validate rating
		for (var r : VALID_RATINGS) {
			if (rating.equals(r)) {
				return;
			}
		}

		throw new RuntimeException("Rating invalid");
	}

}
