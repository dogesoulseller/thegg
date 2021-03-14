package pl.dogesoulseller.thegg.api.response;

import lombok.*;

/**
 * Generic response containing a single message
 */
@Data
public class GenericResponse {
	private final String message;
}
