package pl.dogesoulseller.thegg;

import java.util.LinkedList;
import java.util.List;

public class QueryParser {
	private List<String> includedTags;
	private List<String> excludedTags;

	private String query;

	public QueryParser(String query) {
		includedTags = new LinkedList<>();
		excludedTags = new LinkedList<>();
		this.query = query;
	}

	public void reset() {
		includedTags = new LinkedList<>();
		excludedTags = new LinkedList<>();
	}

	public void reset(String query) {
		includedTags = new LinkedList<>();
		excludedTags = new LinkedList<>();
		this.query = query;
	}

	public QueryParser parse() {
		// TODO: Extended queries (sorting)
		// TODO: Additional specifiers through ~

		// Split at spaces
		String[] tokens = query.replaceAll("\\s+", " ").split(" ");
		for (var tag : tokens) {
			if (tag.startsWith("-")) { // Exclusion
				excludedTags.add(tag.substring(1));
			} else { // Inclusion
				includedTags.add(tag);
			}
		}

		return this;
	}

	public List<String> getExcludedTags() {
		return excludedTags;
	}

	public List<String> getIncludedTags() {
		return includedTags;
	}
}
