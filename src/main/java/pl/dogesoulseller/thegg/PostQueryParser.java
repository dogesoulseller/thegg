package pl.dogesoulseller.thegg;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import pl.dogesoulseller.thegg.Utility.Pair;
import pl.dogesoulseller.thegg.Utility.Triple;

public class PostQueryParser implements QueryParser {
	private List<String> includedTags;
	private List<String> excludedTags;
	private List<Triple<String, String, String>> specialFiltering;
	private Sort sorting;

	private String query;

	public PostQueryParser(String query) {
		includedTags = new LinkedList<>();
		excludedTags = new LinkedList<>();
		this.query = query;
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

	@Override
	public PostQueryParser parse() {
		sorting = Sort.by(new Order(Direction.DESC, "id"));

		// Split at spaces
		String[] tokens = query.replaceAll("\\s+", " ").split(" ");
		for (var tag : tokens) {
			tag = tag.toLowerCase();
			// TODO: Use mongotemplate instead of repo method for this (implement query builder)

			if (tag.startsWith("-")) { // Exclusion
				excludedTags.add(tag.substring(1));
			} else if (tag.startsWith("~")) { // Special parameters
				// FIXME: Move to separate method
				if (tag.contains("sort:")) {
					tag = tag.substring(6).strip();
					sorting = Sort.by(new Order(Direction.DESC, tag));
				} else if (tag.contains("rating:")) {
					tag = tag.substring(9).strip();
					// TODO: Select only specified rating
				} else if (tag.contains("size:")) {
					tag = tag.substring(6).strip();
					// TODO: filter by filesize > < =
				} else if (tag.contains("mime:") || tag.contains("type:")) {
					tag = tag.substring(6).strip();
					// TODO: filter by mime or filetype
				} else if (tag.contains("width:")) {
					tag = tag.substring(7).strip();
					// TODO: filter by width > < =
				} else if (tag.contains("height:")) {
					tag = tag.substring(8).strip();
					// TODO: filter by height > < =
				} else if (tag.contains("date:")) {
					tag = tag.substring(6).strip();
					// TODO: filter by creation date > < =
				} else {
					continue;
				}
			} else { // Inclusion
				includedTags.add(tag);
			}
		}

		return this;
	}

	@Override
	public List<String> getExclusions() {
		return excludedTags;
	}

	@Override
	public List<String> getInclusions() {
		return includedTags;
	}

	@Override
	public Sort getSorting() {
		return sorting;
	}
}
