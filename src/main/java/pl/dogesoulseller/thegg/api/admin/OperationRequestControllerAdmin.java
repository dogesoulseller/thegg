package pl.dogesoulseller.thegg.api.admin;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.oprequest.NewOpRequest;
import pl.dogesoulseller.thegg.api.model.oprequest.OpRequest;
import pl.dogesoulseller.thegg.exception.FieldValidationException;
import pl.dogesoulseller.thegg.exception.UnsupportedActionException;
import pl.dogesoulseller.thegg.service.OpRequestService;
import pl.dogesoulseller.thegg.user.User;

import java.util.List;

import static pl.dogesoulseller.thegg.Utility.authenticateAdminKey;
import static pl.dogesoulseller.thegg.Utility.getServerBaseURL;

@RestController
public class OperationRequestControllerAdmin {
	private final OpRequestService requestService;

	public OperationRequestControllerAdmin(OpRequestService requestService) {
		this.requestService = requestService;
	}

	@ApiOperation(value = "Get active requests", notes = "Get all active (not closed) requests")
	@GetMapping(value = "/api/oprequest/admin/active", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OpRequest>> getActiveRequests(@RequestParam String apikey) {
		if (authenticateAdminKey(apikey) == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		List<OpRequest> activeRequests = requestService.getAllActive().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		return new ResponseEntity<>(activeRequests, HttpStatus.OK);
	}

	@ApiOperation(value = "Update request", notes = "Update the request with new information. Not fully compliant with HTTP spec")
	@PutMapping(value = "/api/admin/oprequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateRequest(@RequestParam String apikey, @RequestBody OpRequest info) {
		User user = authenticateAdminKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		var oldRequestResult = requestService.getRequest(info.getId());

		// Results in making a new request
		if (oldRequestResult.isEmpty()) {
			OpRequest insertedRequest;
			try {
				NewOpRequest newRequest = new NewOpRequest(info.getType(), info.getOperation(), info.getRequestTargetId(), info.getPayload());
				insertedRequest = requestService.makeRequest(user, newRequest);
			} catch (FieldValidationException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed", e);
			} catch (UnsupportedActionException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided combination of type and operation is not supported", e);
			}

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Location", getServerBaseURL() + "/api/oprequest?id=" + insertedRequest.getId());

			return new ResponseEntity<>(null, headers, HttpStatus.CREATED);
		} else {
			OpRequest updatedRequest;

			try {
				updatedRequest = requestService.updateRequest(oldRequestResult.get(), info);
			} catch (FieldValidationException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed", e);
			} catch (UnsupportedActionException e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided combination of type and operation is not supported", e);
			}

			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
			headers.add("Location", getServerBaseURL() + "/api/oprequest?id=" + updatedRequest.getId());

			return new ResponseEntity<>(null, headers, HttpStatus.NO_CONTENT);
		}
	}
}
