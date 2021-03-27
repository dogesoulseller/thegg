package pl.dogesoulseller.thegg.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Generic response containing a single message
 */
@XmlRootElement
public class GenericResponse {
	private final String message;

	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
	public GenericResponse(@JsonProperty  String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public String toString() {
		return "GenericResponse(message=" + this.getMessage() + ")";
	}
}
