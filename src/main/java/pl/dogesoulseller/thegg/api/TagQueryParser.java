// package pl.dogesoulseller.thegg.api;

// import java.util.LinkedList;
// import java.util.List;

// import pl.dogesoulseller.thegg.PostQueryParser;

// public class TagQueryParser {
// 	private List<String> includedTags;
// 	private List<String> excludedTags;
// 	private Sort sorting;

// 	private String query;

// 	public PostQueryParser(String query) {
// 		includedTags = new LinkedList<>();
// 		excludedTags = new LinkedList<>();
// 		this.query = query;
// 	}

// 	@Override
// 	public void reset() {
// 		includedTags = new LinkedList<>();
// 		excludedTags = new LinkedList<>();
// 	}

// 	@Override
// 	public void reset(String query) {
// 		includedTags = new LinkedList<>();
// 		excludedTags = new LinkedList<>();
// 		this.query = query;
// 	}

// 	@Override
// 	public PostQueryParser parse() {
// 		// TODO: Extended queries (sorting)
// 		// TODO: Additional specifiers through ~

// 		sorting = Sort.by(new Order(Direction.DESC, "id"));

// 		// Split at spaces
// 		String[] tokens = query.replaceAll("\\s+", " ").split(" ");
// 		for (var tag : tokens) {
// 			if (tag.startsWith("-")) { // Exclusion
// 				excludedTags.add(tag.substring(1));
// 			} else { // Inclusion
// 				includedTags.add(tag);
// 			}
// 		}

// 		return this;
// 	}

// 	@Override
// 	public List<String> getExclusions() {
// 		return excludedTags;
// 	}

// 	@Override
// 	public List<String> getInclusions() {
// 		return includedTags;
// 	}

// 	@Override
// 	public Sort getSorting() {
// 		return sorting;
// 	}
// }
