package pl.dogesoulseller.thegg.api;

import io.swagger.annotations.Api;
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
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.exception.FieldValidationException;
import pl.dogesoulseller.thegg.exception.UnsupportedActionException;
import pl.dogesoulseller.thegg.exception.UserMismatchException;
import pl.dogesoulseller.thegg.repo.MongoRequestRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.service.OpRequestService;
import pl.dogesoulseller.thegg.user.User;

import java.util.List;

import static pl.dogesoulseller.thegg.Utility.getServerBaseURL;

@Api(tags = "OpRequest")
@RestController
public class OperationRequestController {
	private final ApiKeyVerificationService keyVerifier;

	private final OpRequestService requestService;

	private final MongoRequestRepository requestRepo;

	public OperationRequestController(ApiKeyVerificationService keyVerifier, OpRequestService requestService, MongoRequestRepository requestRepo) {
		this.keyVerifier = keyVerifier;
		this.requestService = requestService;
		this.requestRepo = requestRepo;
	}

	// TODO: Implement admin-related functionality with admin prefix

	@ApiOperation(value = "Get active requests", notes = "Get all active (not closed) requests")
	@GetMapping(value = "/api/oprequest/active", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OpRequest>> getActiveRequests(@RequestParam String apikey) {
		if (!keyVerifier.isAdminValid(apikey)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		List<OpRequest> activeRequests = requestService.getAllActive().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

		return new ResponseEntity<>(activeRequests, HttpStatus.OK);
	}

	@ApiOperation(value = "Get self-submitted request", notes = "Get information about a request. API key must belong to the user who posted the request")
	@GetMapping(value = "/api/oprequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OpRequest> getRequest(@RequestParam String apikey, @RequestParam String id) {
		if (!keyVerifier.isValid(apikey)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		User user = keyVerifier.getKeyUser(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		try {
			var request = requestService.getUserRequest(user, id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			return new ResponseEntity<>(request, HttpStatus.OK);
		} catch (UserMismatchException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
	}

	@ApiOperation(value = "Cancel self-submitted request", notes = "Cancel a request. API key must belong to the user who posted the request")
	@DeleteMapping(value = "/api/oprequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> deleteRequest(@RequestParam String apikey, @RequestParam String id) {
		if (!keyVerifier.isValid(apikey)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		User user = keyVerifier.getKeyUser(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		// User must be the submitting user
		var request = requestRepo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (!request.getRequestUserId().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		request.setResolved(true);
		request.setStatus("USER CANCELLED");

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// TODO: Document possible types and operations and their expected payloads
	@ApiOperation(value = "Make a new request", notes = "Make a new request. See TODO:<types info> and TODO:<operations info> for a description of the allowed values for those fields, and the expected payload type")
	@PostMapping(value = "/api/oprequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> makeRequest(@RequestParam String apikey, @RequestBody NewOpRequest request) {
		if (!keyVerifier.isValid(apikey)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		User user = keyVerifier.getKeyUser(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		OpRequest insertedRequest;

		try {
			insertedRequest = requestService.makeRequest(user, request);
		} catch (FieldValidationException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Validation failed", e);
		} catch (UnsupportedActionException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provided combination of type and operation is not supported", e);
		}

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Location", getServerBaseURL() + "/api/oprequest?id=" + insertedRequest.getId());

		return new ResponseEntity<>(new GenericResponse("Request submitted"), headers, HttpStatus.CREATED);
	}
}
