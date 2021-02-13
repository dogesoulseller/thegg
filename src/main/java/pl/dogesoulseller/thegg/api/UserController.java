package pl.dogesoulseller.thegg.api;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.api.model.UserSelfInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.inputvalidation.PasswordValidator;
import pl.dogesoulseller.thegg.inputvalidation.UserValidator;
import pl.dogesoulseller.thegg.repo.MongoRoleRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.User;
import pl.dogesoulseller.thegg.api.model.UserRegister;

@RestController
public class UserController {
	@Autowired
	private MongoUserRepository userRepository;

	@Autowired
	private MongoRoleRepository roleRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@PostMapping("/api/user")
	@CrossOrigin
	@ResponseBody
	public ResponseEntity<GenericResponse> registerNewUser(@RequestBody UserRegister userdata) {
		String email = userdata.getEmail().toLowerCase();

		if (!UserValidator.validateEmail(email)) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "InvalidEmail");
		}

		if (!UserValidator.validateUsername(userdata.getUsername())) {
			throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "InvalidUsername");
		}

		if (!PasswordValidator.validateUserPassword(userdata.getPassword())) {
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

		userRepository.save(newUser);

		return new ResponseEntity<>(new GenericResponse("Success"), HttpStatus.CREATED);
	}

	@GetMapping("/api/user")
	@CrossOrigin
	@ResponseBody
	public ResponseEntity<UserSelfInfo> getUserInfo() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase();

		User user = userRepository.findByEmail(email);

		// Should technically not happen
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		UserSelfInfo userInfo = new UserSelfInfo(user);

		return new ResponseEntity<>(userInfo, HttpStatus.OK);
	}

	@GetMapping("/api/user/{userinfo}")
	@CrossOrigin
	@ResponseBody
	public ResponseEntity<UserSelfInfo> getUserInfo(@PathVariable String userid) {
		User user;

		if (userid.contains("@")) {
			user = userRepository.findByEmail(userid.toLowerCase());
		} else {
			user = userRepository.findById(userid.toLowerCase()).orElse(null);
		}

		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		UserSelfInfo userInfo = new UserSelfInfo(user);

		if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(userid)) {
			userInfo.setEmail(null);
		}

		return new ResponseEntity<>(userInfo, HttpStatus.OK);
	}

	@PatchMapping("/api/user/{email}")
	@CrossOrigin
	@ResponseBody
	public ResponseEntity<GenericResponse> updateUserInfo(@RequestBody UserSelfInfo info, @PathVariable String email) {
		if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(email)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		// TODO: Use JSESSIONID cookie value for API access

		return new ResponseEntity<>(new GenericResponse(""), HttpStatus.NOT_IMPLEMENTED);
	}
}
