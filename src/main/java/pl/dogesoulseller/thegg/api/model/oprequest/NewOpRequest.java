package pl.dogesoulseller.thegg.api.model.oprequest;

/**
 * Represents user's data about an op request to be created
 */
public class NewOpRequest {
	private String type;
	private String operation;
	private String targetId;
	private String payload;

	public NewOpRequest(String type, String operation, String targetId, String payload) {
		this.type = type;
		this.operation = operation;
		this.targetId = targetId;
		this.payload = payload;
	}

	public String getType() {
		return type;
	}

	public String getOperation() {
		return operation;
	}

	public String getTargetId() {
		return targetId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
}

