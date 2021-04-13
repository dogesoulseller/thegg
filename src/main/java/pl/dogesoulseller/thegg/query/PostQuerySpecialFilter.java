package pl.dogesoulseller.thegg.query;

/**
 * Special filter (query components starting with ~)
 */
public class PostQuerySpecialFilter {
	/**
	 * <, >, or =
	 */
	private final Character comparison;

	/**
	 * Field name, for example - "username"
	 */
	private final String field;

	/**
	 * Value the field is compared to, for example "1000"
	 */
	private final String value;

	public PostQuerySpecialFilter(Character comparison, String field, String value) {
		this.comparison = comparison;
		this.field = field;
		this.value = value;
	}

	public Character getComparison() {
		return comparison;
	}

	public String getField() {
		return field;
	}

	public String getValue() {
		return value;
	}
}
