package pl.dogesoulseller.thegg;

import java.util.List;

import pl.dogesoulseller.thegg.Utility.Triple;

public class PostQueryBuilder {
	public PostQueryBuilder() {

	}

	// TODO: Use Enums
	// Triple<ComparisonOperator, Field, ValueCompared>
	public PostQueryBuilder append(Triple<String, String, String> tag) {
		// TODO: Append as AND operation
		return this;
	}

	public PostQueryBuilder includedTags(List<String> tags) {
		// TODO: Place included tags at start
		return this;
	}

	public PostQueryBuilder excludedTags(List<String> tags) {
		// TODO: Place excluded tags at start
		return this;
	}

	public String finish() {
		// TODO: Assemble
		return "";
	}
}
