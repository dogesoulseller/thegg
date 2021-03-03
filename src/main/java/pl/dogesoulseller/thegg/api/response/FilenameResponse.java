package pl.dogesoulseller.thegg.api.response;

/**
 * Response with filename and URL of next endpoint
 */
public class FilenameResponse {
	private final String filename;
	private final String message;
	private final String next;

	public FilenameResponse(String message, String filename, String next) {
		this.filename = filename;
		this.message = message;
		this.next = next;
	}

	public String getFilename() {
		return filename;
	}

	public String getMessage() {
		return message;
	}
	
	public String getNext() {
		return next;
	}
}
