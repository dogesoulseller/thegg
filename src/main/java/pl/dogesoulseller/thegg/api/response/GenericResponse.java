package pl.dogesoulseller.thegg.api.response;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Generic response containing a single message
 */
@XmlRootElement
public class GenericResponse {
	private final String message;

	public GenericResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public String toString() {
		return "GenericResponse(message=" + this.getMessage() + ")";
	}
}
