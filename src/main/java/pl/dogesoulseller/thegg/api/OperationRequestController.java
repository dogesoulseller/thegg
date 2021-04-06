package pl.dogesoulseller.thegg.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.OpRequest;
import pl.dogesoulseller.thegg.repo.MongoRequestRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;

import java.util.List;

@RestController
public class OperationRequestController {
	private final ApiKeyVerificationService keyVerifier;

	private final MongoRequestRepository requestRepo;

	public OperationRequestController(ApiKeyVerificationService keyVerifier, MongoRequestRepository requestRepo) {
		this.keyVerifier = keyVerifier;
		this.requestRepo = requestRepo;
	}

	@GetMapping(value="/api/oprequest/active", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OpRequest>> getActiveRequests(@RequestParam String apikey) {
		if (!keyVerifier.isAdminValid(apikey)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		var response = requestRepo.findByResolvedFalse();
		if (response == null || response.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(response, HttpStatus.OK);
	}
}
