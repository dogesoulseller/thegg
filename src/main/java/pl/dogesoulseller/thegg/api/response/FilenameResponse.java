package pl.dogesoulseller.thegg.api.response;


import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response with filename and URL of next endpoint
 */
@XmlRootElement
public class FilenameResponse {
	private final String filename;
	private final String message;
	private final String next;

	public FilenameResponse(String filename, String message, String next) {
		this.filename = filename;
		this.message = message;
		this.next = next;
	}

	public String getFilename() {
		return this.filename;
	}

	public String getMessage() {
		return this.message;
	}

	public String getNext() {
		return this.next;
	}

	public String toString() {
		return "FilenameResponse(filename=" + this.getFilename() + ", message=" + this.getMessage() + ", next=" + this.getNext() + ")";
	}
}
