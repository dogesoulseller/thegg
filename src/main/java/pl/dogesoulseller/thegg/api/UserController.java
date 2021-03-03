package pl.dogesoulseller.thegg.api;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import pl.dogesoulseller.thegg.Utility;
import pl.dogesoulseller.thegg.api.model.UserRegister;
import pl.dogesoulseller.thegg.api.model.UserSelfInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.inputvalidation.PasswordValidator;
import pl.dogesoulseller.thegg.inputvalidation.UserValidator;
import pl.dogesoulseller.thegg.repo.MongoRoleRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.user.User;

@Api(tags = {"User"})
@RestController
public class UserController {
	@Autowired
	private MongoUserRepository userRepository;

	@Autowired
	private MongoRoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private PasswordValidator passwordValidator;

	@Autowired
	private UserValidator userValidator;

	@Autowired
	private ApiKeyVerificationService keyVerifier;

	@ApiOperation(value = "Register user", notes = "Register a new user")
	@PostMapping("/api/user")
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
				roleRepository.findByName("ROLE_USER"), Instant.now());

		User insertedUser = userRepository.save(newUser);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Location", Utility.getServerBaseURL() + "/api/user/" + insertedUser.getId());

		return new ResponseEntity<>(new GenericResponse("Success"), HttpStatus.CREATED);
	}

	@ApiOperation(value = "Get user info", notes = "Gets user info that is stripped of all private data<br><br>If userid is not specified, the API key's owner's data is returned")
	@GetMapping("/api/user")
	public ResponseEntity<UserSelfInfo> getUserInfo(@RequestParam String apikey, @RequestParam(required = false) String userid) {
		User requestUser = keyVerifier.getKeyUser(apikey);
		if (requestUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		// Request with no userid returns current user info
		if (userid == null) {
			UserSelfInfo selfInfo = new UserSelfInfo(requestUser);

			return new ResponseEntity<>(selfInfo, HttpStatus.OK);
		} else {
			User user = userRepository.findById(userid.toLowerCase()).orElse(null);
			if (user == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND);
			}

			UserSelfInfo userInfo = new UserSelfInfo(user);

			// Set email info only if API key holder is also user searched for
			if (requestUser.getId() == user.getId()) {
				userInfo.setEmail(requestUser.getUsername());
			}

			return new ResponseEntity<>(userInfo, HttpStatus.OK);
		}
	}

	@ApiOperation(value = "Update user", notes = "Update API key holder's user profile with provided info")
	@PatchMapping("/api/user")
	public ResponseEntity<GenericResponse> updateUserInfo(@RequestParam String apikey,
		@RequestBody UserSelfInfo info) {
		User requestUser = keyVerifier.getKeyUser(apikey);
		if (requestUser == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		requestUser.update(info);

		userRepository.save(requestUser);

		return new ResponseEntity<>(new GenericResponse("Updated"), HttpStatus.OK);
	}
}
