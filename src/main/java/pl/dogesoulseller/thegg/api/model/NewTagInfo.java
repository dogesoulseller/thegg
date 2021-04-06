package pl.dogesoulseller.thegg.api.model;

public class NewTagInfo {
	private String tag;
	private String description;

	public NewTagInfo(String tag, String description) {
		this.tag = tag;
		this.description = description;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
