package pl.dogesoulseller.thegg.api.model.oprequest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Op request payload related to reports
 */
public class ReportReason {
	private String reason;

	@JsonCreator
	public ReportReason(@JsonProperty("reason") String reason) {
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
