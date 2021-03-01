package pl.dogesoulseller.thegg;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.domain.Sort;

public class PostQueryParser implements QueryParser {
	private List<String> includedTags;
	private List<String> excludedTags;

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
		// TODO Auto-generated method stub
		return null;
	}
}
