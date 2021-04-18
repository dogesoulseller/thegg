package pl.dogesoulseller.thegg.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;
import pl.dogesoulseller.thegg.api.model.oprequest.NewOpRequest;
import pl.dogesoulseller.thegg.api.model.oprequest.OpRequest;
import pl.dogesoulseller.thegg.api.model.oprequest.ReportReason;
import pl.dogesoulseller.thegg.exception.FieldValidationException;
import pl.dogesoulseller.thegg.exception.UnsupportedActionException;
import pl.dogesoulseller.thegg.exception.UserMismatchException;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.repo.MongoRequestRepository;
import pl.dogesoulseller.thegg.user.User;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service handling the management of op requests
 */
@Service
public class OpRequestService {
	private final MongoRequestRepository requestRepo;
	private final MongoPostRepository postRepo;

	private final HashMap<String, Map<String, Class<?>>> supportMap;

	public OpRequestService(MongoRequestRepository requestRepo, MongoPostRepository postRepo) {
		this.requestRepo = requestRepo;
		this.postRepo = postRepo;

		supportMap = new HashMap<>();
		supportMap.put("post", Map.of("report", ReportReason.class));
		supportMap.put("tag", Map.of("report", ReportReason.class));
	}

	public Optional<List<OpRequest>> getAllActive() {
		var response = requestRepo.findByResolvedFalse();
		if (response == null || response.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(response);
	}

	public Optional<OpRequest> getRequest(String requestId) {
		if (requestId == null) {
			return Optional.empty();
		}

		return requestRepo.findById(requestId);
	}

	public Optional<OpRequest> getUserRequest(User user, String requestId) throws UserMismatchException {
		var request = requestRepo.findById(requestId);
		if (request.isEmpty()) {
			return Optional.empty();
		}

		if (!request.get().getRequestUserId().equals(user.getId())) {
			throw new UserMismatchException("Request user and key user are different");
		}

		return request;
	}

	private boolean validatePayload(Object payload, Class<?> target) {
		if (target == ReportReason.class) {
			return ((ReportReason) payload).getReason() != null;
		}

		return true;
	}

	private boolean validateRequestTarget(String targetId, String operationType) {
		if (operationType.equals("post")) {
			return postRepo.existsById(targetId);
		}

		return false;
	}

	public void cancelUserRequest(User user, OpRequest request) throws UserMismatchException {
		if (!request.getRequestUserId().equals(user.getId())) {
			throw new UserMismatchException("Request user and key user are different");
		}

		request.setResolved(true);
		request.setStatus("USER CANCELLED");

		requestRepo.save(request);
	}

	private String validateType(String type) throws FieldValidationException, UnsupportedActionException {
		if (type == null || type.isBlank()) {
			throw new FieldValidationException("Type must not be blank");
		} else {
			type = type.strip().toLowerCase();
		}

		if (!supportMap.containsKey(type)) {
			throw new UnsupportedActionException("Type " + type + " is not supported");
		}

		return type;
	}

	private String validateOperation(String operation, String type) throws FieldValidationException, UnsupportedActionException {
		if (operation == null || operation.isBlank()) {
			throw new FieldValidationException("Operation must not be blank");
		} else {
			operation = operation.strip().toLowerCase();
		}

		if (!supportMap.get(type).containsKey(operation)) {
			throw new UnsupportedActionException("Operation" + operation + " for type " + type + " is not supported");
		}

		return operation;
	}

	public OpRequest updateRequest(OpRequest oldRequestData, OpRequest newRequestData) throws FieldValidationException, UnsupportedActionException {
		oldRequestData.update(newRequestData);

		String type = validateType(oldRequestData.getType());
		String operation = validateOperation(oldRequestData.getOperation(), oldRequestData.getType());

		oldRequestData.setType(type);
		oldRequestData.setOperation(operation);

		return requestRepo.save(oldRequestData);
	}

	public OpRequest makeRequest(User user, NewOpRequest request) throws FieldValidationException, UnsupportedActionException {
		// Validate type
		String type = validateType(request.getType());
		var supportedTypeOps = supportMap.get(type);

		// Validate operation
		String operation = validateOperation(request.getOperation(), type);
		var opType = supportedTypeOps.get(operation);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());

		try {
			var pType = mapper.readValue(request.getPayload(), opType);
			if (!validatePayload(pType, opType) || !validateRequestTarget(request.getTargetId(), type)) {
				throw new FieldValidationException("Payload invalid");
			}
		} catch (JsonProcessingException e) {
			throw new FieldValidationException("Payload parsing failed", e);
		}

		OpRequest newRequest = new OpRequest(null, type, operation, user.getId(), request.getTargetId(), Instant.now(), request.getPayload(), false, "NEW");

		return requestRepo.insert(newRequest);
	}
}
