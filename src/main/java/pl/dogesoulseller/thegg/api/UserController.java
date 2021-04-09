package pl.dogesoulseller.thegg.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.UserRegister;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.api.response.UserSelfInfo;
import pl.dogesoulseller.thegg.inputvalidation.PasswordValidator;
import pl.dogesoulseller.thegg.inputvalidation.UserValidator;
import pl.dogesoulseller.thegg.repo.MongoRoleRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.user.User;

import java.time.Instant;

import static pl.dogesoulseller.thegg.Utility.getServerBaseURL;

@Api(tags = "User")
@RestController
public class UserController {
	private final MongoUserRepository userRepository;

	private final MongoRoleRepository roleRepository;

	private final PasswordEncoder passwordEncoder;

	private final PasswordValidator passwordValidator;

	private final UserValidator userValidator;

	private final ApiKeyVerificationService keyVerifier;

	public UserController(MongoUserRepository userRepository, MongoRoleRepository roleRepository, PasswordEncoder passwordEncoder, PasswordValidator passwordValidator, UserValidator userValidator, ApiKeyVerificationService keyVerifier) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.passwordValidator = passwordValidator;
		this.userValidator = userValidator;
		this.keyVerifier = keyVerifier;
	}

	@ApiOperation(value = "Register user", notes = "Register a new user")
	@PostMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> registerNewUser(@RequestBody UserRegister userdata) {
		String email = userdata.getEmail().toLowerCase();

		if (!userValidator.validateEmail(email)) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "InvalidEmail");
		}

		if (!userValidator.validateUsername(userdata.getUsername())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "InvalidUsername");
		}

		if (!passwordValidator.validateUserPassword(userdata.getPassword())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "PasswordShort");
		}

		if (!userdata.getPassword().equals(userdata.getPasswordConfirm())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "PasswordMismatch");
		}

		if (userRepository.findByEmail(email) != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate");
		}

		// TODO: Email verification
		User newUser = new User(email, userdata.getUsername(), passwordEncoder.encode(userdata.getPassword()),
			roleRepository.findByName("ROLE_USER").orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "User role does not exist")), Instant.now());

		User insertedUser = userRepository.save(newUser);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Location", getServerBaseURL() + "/api/user?userid=" + insertedUser.getId());

		return new ResponseEntity<>(new GenericResponse("Success"), headers, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get user info", notes = "Gets user info that is stripped of all private data<br><br>If userid is not specified, the API key's owner's data is returned")
	@GetMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<UserSelfInfo> getUserInfo(@RequestParam String apikey, @RequestParam(required = false) String userid) {
		User requestUser = keyVerifier.getKeyUser(apikey);
		if (requestUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		// Request with no userid returns current user info
		if (userid == null) {
			UserSelfInfo selfInfo = new UserSelfInfo(requestUser);
			selfInfo.setEmail(requestUser.getEmail());

			return new ResponseEntity<>(selfInfo, HttpStatus.OK);
		} else {
			User user = userRepository.findById(userid.toLowerCase()).orElse(null);
			if (user == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find user with id " + userid);
			}

			UserSelfInfo userInfo = new UserSelfInfo(user);

			// Set email info only if API key holder is also user searched for
			if (requestUser.getId().equals(user.getId())) {
				userInfo.setEmail(requestUser.getUsername());
			}

			return new ResponseEntity<>(userInfo, HttpStatus.OK);
		}
	}

	private void validateAndSaveUser(User user) {
		if (user.getEmail() == null || !userValidator.validateEmail(user.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be a valid address");
		}

		if (user.getNonUniqueUsername() == null || !userValidator.validateUsername(user.getNonUniqueUsername())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be a valid username");
		}

		userRepository.save(user);
	}

	@ApiOperation(value = "Update user", notes = "Update API key holder's user profile with provided info. Fields with null values are ignored")
	@PatchMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> updateUserInfo(@RequestParam String apikey, @RequestBody UserSelfInfo info) {
		User requestUser = keyVerifier.getKeyUser(apikey);
		if (requestUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		requestUser.update(info);

		requestUser.setEmail(requestUser.getEmail().toLowerCase());

		validateAndSaveUser(requestUser);

		return new ResponseEntity<>(new GenericResponse("Updated"), HttpStatus.OK);
	}

	@ApiOperation(value = "Update user", notes = "Update API key holder's user profile with provided info")
	@PutMapping(value = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> updateUserInfoFull(@RequestParam String apikey, @RequestBody UserSelfInfo info) {
		info.setEmail(info.getEmail().toLowerCase());

		User requestUser = keyVerifier.getKeyUser(apikey);
		if (requestUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		requestUser.updateFull(info);

		requestUser.setEmail(requestUser.getEmail().toLowerCase());

		validateAndSaveUser(requestUser);

		return new ResponseEntity<>(new GenericResponse("Updated"), HttpStatus.OK);
	}
}
