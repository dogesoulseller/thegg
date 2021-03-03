package pl.dogesoulseller.thegg;

/**
 * Special filter (query components starting with ~)
 */
public class PostQuerySpecialFilter {
	/**
	 * <, >, or =
	 */
	private Character comparison;

	/**
	 * Field name, for example - "username"
	 */
	private String field;

	/**
	 * Value the field is compared to, for example "1000"
	 */
	private String value;

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
