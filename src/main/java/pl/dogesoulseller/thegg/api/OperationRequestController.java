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
import pl.dogesoulseller.thegg.service.OpRequestService;
import pl.dogesoulseller.thegg.user.User;

import static pl.dogesoulseller.thegg.Utility.*;

@Api(tags = "OpRequest")
@RestController
public class OperationRequestController {
	private final OpRequestService requestService;

	public OperationRequestController(OpRequestService requestService) {
		this.requestService = requestService;
	}

	@ApiOperation(value = "Get self-submitted request", notes = "Get information about a request. API key must belong to the user who posted the request")
	@GetMapping(value = "/api/oprequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OpRequest> getRequest(@RequestParam String apikey, @RequestParam String id) {
		User user = authenticateUserKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		try {
			var request = requestService.getUserRequest(user, id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			return new ResponseEntity<>(request, HttpStatus.OK);
		} catch (UserMismatchException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, null, e);
		}
	}

	@ApiOperation(value = "Cancel self-submitted request", notes = "Cancel a request. API key must belong to the user who posted the request")
	@DeleteMapping(value = "/api/oprequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> deleteRequest(@RequestParam String apikey, @RequestParam String id) {
		User user = authenticateUserKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		try {
			var request = requestService.getUserRequest(user, id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
			requestService.cancelUserRequest(user, request);
		} catch (UserMismatchException e) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, null, e);
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	// TODO: Document possible types and operations and their expected payloads
	@ApiOperation(value = "Make a new request", notes = "Make a new request. See TODO:<types info> and TODO:<operations info> for a description of the allowed values for those fields, and the expected payload type")
	@PostMapping(value = "/api/oprequest", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> makeRequest(@RequestParam String apikey, @RequestBody NewOpRequest request) {
		User user = authenticateUserKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
