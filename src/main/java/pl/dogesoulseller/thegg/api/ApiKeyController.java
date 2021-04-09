package pl.dogesoulseller.thegg.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.api.response.SelfApiKeyInfo;
import pl.dogesoulseller.thegg.repo.MongoKeyRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.ApiKey;
import pl.dogesoulseller.thegg.user.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Api(tags = "API Keys")
@RestController
public class ApiKeyController {
	private final MongoKeyRepository keyRepo;

	private final MongoUserRepository userRepo;

	public ApiKeyController(MongoKeyRepository keyRepo, MongoUserRepository userRepo) {
		this.keyRepo = keyRepo;
		this.userRepo = userRepo;
	}

	/**
	 * Get user from current session
	 *
	 * @return current user
	 * @throws Exception on authentication failed
	 */
	private User getRequestUser() throws Exception {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new Exception("No authentication passed");
		}

		var userEmail = auth.getName();
		if (userEmail == null || (!userEmail.contains("@"))) {
			throw new Exception("User's username points to a temporary session");
		}

		return userRepo.findByEmail(auth.getName());
	}

	@ApiOperation(value = "Generate new API key", notes = "Creates a new API key attached to the user account.<br><br>Note: This method requires an initial login through POST on /login with username=EMAIL and password=PASSWORD. The resulting JSESSIONID cookie must be set on this request.")
	@PostMapping(value = "/api/apikey", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> generateNewKey(
		@ApiParam(value = "Unique name to use for the API key", defaultValue = "default") @RequestParam(required = false) String name) {
		name = name == null ? "default" : name;

		User user;
		try {
			user = getRequestUser();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized", e);
		}

		// Error if at least 5 keys already exist
		if (keyRepo.countByUseridAndActive(user.getId(), true) >= 5) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Tried to create more keys than allowed");
		}

		// Error if key name already exists
		if (keyRepo.existsByNameAndUserid(name, user.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A key by this name already exists");
		}

		// Generate key repeatedly in case a non-random UUID is generated (extremely low
		// chance)
		String generatedKey;
		do {
			generatedKey = UUID.randomUUID().toString().replace("-", "");
		} while (keyRepo.existsByKey(generatedKey));

		var outputKey = new ApiKey(generatedKey, name, user.getId());

		keyRepo.insert(outputKey);

		return new ResponseEntity<>(new GenericResponse(generatedKey), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Revoke API key", notes = "Revokes the API key attached to the user account.<br><br>Note: This method requires an initial login through POST on /login with username=EMAIL and password=PASSWORD. The resulting JSESSIONID cookie must be set on this request.")
	@DeleteMapping(value = "/api/apikey", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> removeKey(@RequestParam(required = false) String name) {
		name = name == null ? "default" : name;

		User user;
		try {
			user = getRequestUser();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized", e);
		}

		// Removing an inactive key should return a 404
		var key = keyRepo.findByNameAndUseridAndActive(name, user.getId(), true);
		if (key == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find key with name " + name);
		}

		key.setActive(false);

		keyRepo.save(key);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping(value = "/api/apikey", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SelfApiKeyInfo>> getKey(@RequestParam(required = false) String name) {
		User user;

		try {
			user = getRequestUser();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized", e);
		}

		// Not specifying a name returns all keys for user
		if (name == null) {
			var keys = keyRepo.findByUserid(user.getId());
			if (keys.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			List<SelfApiKeyInfo> infos = keys.stream().map((ApiKey elem)
				-> new SelfApiKeyInfo(elem.getName(), elem.getCreationtime(),
				elem.isActive())).collect(Collectors.toList());

			return new ResponseEntity<>(infos, HttpStatus.OK);
		} else {
			var key = keyRepo.findByNameAndUserid(name, user.getId());
			if (key == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find key with name " + name);
			}

			var keyInfo = new SelfApiKeyInfo(key.getName(), key.getCreationtime(), key.isActive());
			return new ResponseEntity<>(List.of(keyInfo), HttpStatus.OK);
		}
	}
}
