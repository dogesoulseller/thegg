package pl.dogesoulseller.thegg.api.response;

public class GenericResponse {
	private final String message;

	public GenericResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

}
