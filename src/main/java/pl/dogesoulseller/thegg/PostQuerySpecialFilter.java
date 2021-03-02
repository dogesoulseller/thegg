package pl.dogesoulseller.thegg;

public class PostQuerySpecialFilter {
	private Character comparison;
	private String field;
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
