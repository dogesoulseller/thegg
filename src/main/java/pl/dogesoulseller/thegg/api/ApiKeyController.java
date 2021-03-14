package pl.dogesoulseller.thegg.api;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import pl.dogesoulseller.thegg.api.model.SelfApiKeyInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoKeyRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.ApiKey;
import pl.dogesoulseller.thegg.user.User;

@Api(tags = { "API Keys" })
@RestController
public class ApiKeyController {
	@Autowired
	MongoKeyRepository keyRepo;

	@Autowired
	MongoUserRepository userRepo;

	/**
	 * Get user from current session
	 *
	 * @return current user
	 * @throws Exception
	 */
	private User getRequestUser() throws Exception {
		var auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new Exception();
		}

		var userEmail = auth.getName();
		if (userEmail == null || (!userEmail.contains("@"))) {
			throw new Exception();
		}

		return userRepo.findByEmail(auth.getName());
	}

	@ApiOperation(value = "Generate new API key", notes = "Creates a new API key attached to the user account.<br><br>Note: This method requires an initial login through POST on /login with username=EMAIL and password=PASSWORD. The resulting JSESSIONID cookie must be set on this request.")
	@PostMapping("/api/apikey")
	public ResponseEntity<GenericResponse> generateNewKey(
			@ApiParam(value = "Unique name to use for the API key", defaultValue = "default") @RequestParam(required = false) String name) {
		name = name == null ? "default" : name;

		User user;
		try {
			user = getRequestUser();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
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

		return new ResponseEntity<GenericResponse>(new GenericResponse(generatedKey), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Revoke API key", notes = "Revokes the API key attached to the user account.<br><br>Note: This method requires an initial login through POST on /login with username=EMAIL and password=PASSWORD. The resulting JSESSIONID cookie must be set on this request.")
	@DeleteMapping("/api/apikey")
	public ResponseEntity<Object> removeKey(@RequestParam(required = false) String name) {
		name = name == null ? "default" : name;

		User user;
		try {
			user = getRequestUser();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		var key = keyRepo.findByNameAndUserid(name, user.getId());
		if (key == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		key.setActive(false);

		keyRepo.save(key);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@GetMapping("/api/apikey")
	public ResponseEntity<List<SelfApiKeyInfo>> getKey(@RequestParam(required = false) String name) {
		User user;

		try {
			user = getRequestUser();
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		// Not specifying a name returns all keys for user
		if (name == null) {
			var keys = keyRepo.findByUserid(user.getId());
			if (keys.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			List<SelfApiKeyInfo> infos = keys.stream().map((ApiKey elem) -> {
				SelfApiKeyInfo info = new SelfApiKeyInfo(elem.getName(), elem.getCreationtime(), elem.getActive());
				return info;
			}).collect(Collectors.toList());

			return new ResponseEntity<>(infos, HttpStatus.OK);
		} else {
			var key = keyRepo.findByNameAndUserid(name, user.getId());
			if (key == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}

			var keyInfo = new SelfApiKeyInfo(key.getName(), key.getCreationtime(), key.getActive());
			return new ResponseEntity<>(List.of(keyInfo), HttpStatus.OK);
		}
	}
}
