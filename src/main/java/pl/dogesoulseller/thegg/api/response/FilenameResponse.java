package pl.dogesoulseller.thegg.api.response;

import lombok.*;

/**
 * Response with filename and URL of next endpoint
 */
@Data
public class FilenameResponse {
	private final String filename;
	private final String message;
	private final String next;
}
