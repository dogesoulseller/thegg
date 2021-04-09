package pl.dogesoulseller.thegg.api.model.oprequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Represents a single op request stored on a database
 */
@Document(collection = "requests")
public class OpRequest {
	@Id
	private String id;

	@Indexed
	private String type;

	@Indexed
	private String operation;

	@Field("request_user_id")
	private String requestUserId;

	@Field("request_target_id")
	private String requestTargetId;

	@Field("submission_date")
	private Instant submissionDate;

	private String payload;

	@Indexed
	private Boolean resolved;

	private String status;

	@PersistenceConstructor
	@JsonCreator
	public OpRequest(@JsonProperty("id") String id, @JsonProperty("type") String type, @JsonProperty("operation") String operation,
					 @JsonProperty("requestUserId") String requestUserId, @JsonProperty("requestTargetId") String requestTargetId,
					 @JsonProperty("submissionDate") Instant submissionDate,
					 @JsonProperty("payload") String payload, @JsonProperty("resolved") Boolean resolved, @JsonProperty("status") String status) {
		this.id = id;
		this.type = type;
		this.operation = operation;
		this.requestUserId = requestUserId;
		this.requestTargetId = requestTargetId;
		this.submissionDate = submissionDate;
		this.payload = payload;
		this.resolved = resolved;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public String getOperation() {
		return operation;
	}

	public String getRequestUserId() {
		return requestUserId;
	}

	public String getRequestTargetId() {
		return requestTargetId;
	}

	public Instant getSubmissionDate() {
		return submissionDate;
	}

	public Boolean getResolved() {
		return resolved;
	}

	public String getStatus() {
		return status;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public void setRequestUserId(String requestUserId) {
		this.requestUserId = requestUserId;
	}

	public void setRequestTargetId(String requestTargetId) {
		this.requestTargetId = requestTargetId;
	}

	public void setSubmissionDate(Instant submissionDate) {
		this.submissionDate = submissionDate;
	}

	public void setResolved(Boolean resolved) {
		this.resolved = resolved;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}
}
