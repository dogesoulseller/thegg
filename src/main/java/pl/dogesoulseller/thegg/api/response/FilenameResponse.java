package pl.dogesoulseller.thegg.api.response;

public class FilenameResponse {
	private final String filename;
	private final String message;

	public FilenameResponse(String message, String filename) {
		this.filename = filename;
		this.message = message;
	}

	public String getFilename() {
		return filename;
	}

	public String getMessage() {
		return message;
	}
}
