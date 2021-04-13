package pl.dogesoulseller.thegg.query;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Base parser for the post query syntax
 */
public class PostQueryParser implements QueryParser {

	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
	/**
	 * Tags that must be present in the post
	 */
	private List<String> includedTags;

	/**
	 * Tags that must not be present in the post
	 */
	private List<String> excludedTags;

	/**
	 * Special filtering rules
	 *
	 * @see PostQuerySpecialFilter
	 */
	private final List<PostQuerySpecialFilter> specialFiltering;

	/**
	 * Sorting rules
	 */
	private Sort sorting = Sort.by(new Order(Direction.DESC, "id"));

	private String query;

	public PostQueryParser(String query) {
		includedTags = new LinkedList<>();
		excludedTags = new LinkedList<>();
		this.query = query;

		specialFiltering = new LinkedList<>();
	}

	@Override
	public void reset() {
		includedTags = new LinkedList<>();
		excludedTags = new LinkedList<>();
	}

	@Override
	public void reset(String query) {
		includedTags = new LinkedList<>();
		excludedTags = new LinkedList<>();
		this.query = query;
	}

	/**
	 * Parse a single special tag
	 *
	 * @param tag tag
	 * @return filter criteria
	 */
	private PostQuerySpecialFilter parseSpecialTag(String tag) {
		Character sort;
		String field;
		String value;

		if (tag.contains("sort:")) {
			tag = tag.substring(5).strip();
			int separatorIdx = tag.indexOf(':');

			Direction direction;
			try {
				if (separatorIdx == -1) {
					direction = Direction.DESC;
				} else {
					String directionString = tag.substring(separatorIdx).strip().toLowerCase();
					direction = directionString.contains("asc") ? Direction.ASC : Direction.DESC;
				}
			} catch (IndexOutOfBoundsException e) {
				direction = Direction.DESC;
			}

			sorting = Sort.by(new Order(direction, tag));
			return null;
		} else if (tag.contains("rating:")) {
			tag = tag.substring(8).strip();

			sort = null;
			field = "rating";
			value = tag.substring(1).strip();
		} else if (tag.contains("size:")) {
			tag = tag.substring(6).strip();

			sort = tag.charAt(0);
			field = "filesize";
			value = tag.substring(1).strip();
		} else if (tag.contains("mime:") || tag.contains("type:")) {
			tag = tag.substring(6).strip();

			sort = null;
			field = "mime";
			value = tag;
		} else if (tag.contains("width:")) {
			tag = tag.substring(7).strip();

			sort = tag.charAt(0);
			field = "width";
			value = tag.substring(1).strip();
		} else if (tag.contains("height:")) {
			tag = tag.substring(8).strip();

			sort = tag.charAt(0);
			field = "height";
			value = tag.substring(1).strip();
		} else if (tag.contains("date:")) {
			tag = tag.substring(6).strip();

			sort = tag.charAt(0);
			field = "creation_date";
			value = tag.substring(1).strip();
		} else {
			throw new RuntimeException("Unknown field in " + tag);
		}

		return new PostQuerySpecialFilter(sort, field, value);
	}

	/**
	 * Parse the contained query
	 */
	@Override
	public PostQueryParser parse() {
		// Split at spaces
		String[] tokens = WHITESPACE_PATTERN.matcher(query).replaceAll(" ").split(" ");
		for (var tag : tokens) {
			String lcTag = tag.toLowerCase();

			if (lcTag.startsWith("-")) { // Exclusion
				excludedTags.add(lcTag.substring(1));
			} else if (lcTag.startsWith("~")) { // Special parameters
				var specialTag = parseSpecialTag(lcTag);
				if (specialTag != null) {
					specialFiltering.add(specialTag);
				}

			} else { // Inclusion
				includedTags.add(lcTag);
			}
		}

		return this;
	}

	@Override
	public List<String> getExclusions() {
		return Collections.unmodifiableList(excludedTags);
	}

	@Override
	public List<String> getInclusions() {
		return Collections.unmodifiableList(includedTags);
	}

	@Override
	public Sort getSorting() {
		return sorting;
	}

	public List<PostQuerySpecialFilter> getSpecialFiltering() {
		return Collections.unmodifiableList(specialFiltering);
	}
}
