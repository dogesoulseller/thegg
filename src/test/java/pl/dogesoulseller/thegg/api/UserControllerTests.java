package pl.dogesoulseller.thegg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import pl.dogesoulseller.thegg.Session;
import pl.dogesoulseller.thegg.api.model.UserRegister;
import pl.dogesoulseller.thegg.api.model.selfdata.UserSelfInfo;
import pl.dogesoulseller.thegg.repo.MongoRoleRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.Role;
import pl.dogesoulseller.thegg.user.User;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static pl.dogesoulseller.thegg.TestUtility.basicHeaders;
import static pl.dogesoulseller.thegg.TestUtility.cookieHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTests {
	@LocalServerPort
	int serverPort;
	@Autowired
	private MongoRoleRepository roleRepo;
	@Autowired
	private MongoUserRepository userRepo;
	@Autowired
	private TestRestTemplate restTemplate;

	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	private static ConcurrentLinkedDeque<Session> sessions;

	@BeforeAll
	public static void init() {
		sessions = new ConcurrentLinkedDeque<>();
	}

	@AfterAll
	public static void deinit() {
		for (var sess : sessions) {
			sess.close();
		}
	}

	private ResponseEntity<String> register(UserRegister regInfo) {
		try {
			roleRepo.save(new Role("ROLE_USER", List.of("TestPrivilege")));
		} catch (DuplicateKeyException ignored) {
		}

		Session session = new Session(restTemplate.getRestTemplate(), serverPort);
		sessions.add(session);

		HttpHeaders headers = cookieHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, session.getSessionCookie());

		String regDataJson = null;

		User existingUser = userRepo.findByEmail(regInfo.getEmail());
		if (existingUser != null) {
			userRepo.deleteById(existingUser.getId());
		}

		try {
			regDataJson = new ObjectMapper().writeValueAsString(regInfo);
		} catch (JsonProcessingException e) {
			fail("Failed to process user registration data", e);
		}

		return restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/user",
			new HttpEntity<>(regDataJson, headers),
			String.class);

	}

	@Test
	public void registerUser() {
		var response = register(
			new UserRegister("testuser_registeruser@doge.com", "testuser_registeruser", "password123456", "password123456"));

		String[] locationSplit = Objects.requireNonNull(response.getHeaders().get("Location")).get(0).split("/");
		String userid = locationSplit[locationSplit.length - 1];

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotBlank();
		assertThat(userRepo.findByEmail("testuser_registeruser@doge.com")).isNotNull();

		userRepo.deleteById(userid);
	}

	@Test
	public void registerUserFailPassMismatch() {
		var response = register(
			new UserRegister("testuser_registeruserpassmismatch@doge.com", "testuser_registeruser", "password123456", "password123"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
		assertThat(response.getBody()).isNotBlank();
		assertThat(userRepo.findByEmail("testuser_registeruserpassmismatch@doge.com")).isNull();
	}

	@Test
	public void registerUserFailUsernameInvalid() {
		var response = register(
			new UserRegister("testuser_registeruserunamebad@doge.com",
				"testuser_registeruserbutthisoneislikereallyreallylonglikewow", "password123456", "password123456"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		assertThat(response.getBody()).isNotBlank();
		assertThat(userRepo.findByEmail("testuser_registeruserunamebad@doge.com")).isNull();

		var responseShort = register(
			new UserRegister("testuser_registeruserunamebad@doge.com",
				"tu", "password123456", "password123456"));

		assertThat(responseShort.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		assertThat(responseShort.getBody()).isNotBlank();
		assertThat(userRepo.findByEmail("testuser_registeruserunamebad@doge.com")).isNull();
	}

	@Test
	public void registerUserFailPassShort() {
		var response = register(
			new UserRegister("testuser_registeruserpassshort@doge.com", "testuser_registeruser", "pass", "pass"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		assertThat(response.getBody()).isNotBlank();
		assertThat(userRepo.findByEmail("testuser_registeruserpassshort@doge.com")).isNull();
	}

	@Test
	public void registerUserFailEmailInvalid() {
		var response = register(
			new UserRegister("testuser_registeruserbademl@doge.c", "testuser_registeruser", "password123456", "password123456"));

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
		assertThat(response.getBody()).isNotBlank();
		assertThat(userRepo.findByEmail("testuser_registeruserbademl@doge.c")).isNull();
	}

	@Test
	public void getUserInfoOther() {
		Session session = new Session(restTemplate.getRestTemplate(), serverPort);
		sessions.add(session);

		var regResponse = register(new UserRegister("testuser_getuser@doge.com", "testuser_getuser", "password123456", "password123456"));

		assertThat(regResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(regResponse.getBody()).isNotBlank();

		String[] locationSplit = Objects.requireNonNull(regResponse.getHeaders().get("Location")).get(0).split("\\?userid=");
		String userid = locationSplit[1];
		String requestDiffString = "http://localhost:" + serverPort + "/api/user?apikey=" + session.getCredentialManager().getUserKey().getKey() + "&userid=" + userid;
		var responseOther = restTemplate.getForEntity(requestDiffString, UserSelfInfo.class);

		assertThat(responseOther.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseOther.getBody()).isNotNull();
		assertThat(responseOther.getBody().getBio()).isNull();
		assertThat(responseOther.getBody().getEmail()).isNull();
		assertThat(responseOther.getBody().getUsername()).isEqualTo("testuser_getuser");

		userRepo.deleteById(userid);
	}

	@Test
	public void getUserInfoSelf() {
		Session session = new Session(restTemplate.getRestTemplate(), serverPort);
		sessions.add(session);

		// Check for same user
		String requestString = "http://localhost:" + serverPort + "/api/user?apikey=" + session.getCredentialManager().getUserKey().getKey();
		var responseSelf = restTemplate.getForEntity(requestString, UserSelfInfo.class);

		assertThat(responseSelf.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseSelf.getBody()).isNotNull();
		assertThat(responseSelf.getBody().getBio()).isNull();
		assertThat(responseSelf.getBody().getEmail()).isEqualTo(session.getCredentialManager().getUserUser().getEmail());
		assertThat(responseSelf.getBody().getUsername()).isEqualTo(session.getCredentialManager().getUserUser().getNonUniqueUsername());
	}

	@Test
	public void modifyUserInfo() {
		Session session = new Session(restTemplate.getRestTemplate(), serverPort);
		sessions.add(session);

		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		// Get user information pre-update
		User user = Objects.requireNonNull(userRepo.findByEmail(session.getCredentialManager().getUserUser().getEmail()));
		assertThat(user.getBio()).isNull();

		// Update user
		String requestString = "http://localhost:" + serverPort + "/api/user?apikey=" + session.getCredentialManager().getUserKey().getKey();
		UserSelfInfo updateInfo = new UserSelfInfo(null, null, "testbio", null);

		String regDataJson = null;

		try {
			regDataJson = new ObjectMapper().writeValueAsString(updateInfo);
		} catch (JsonProcessingException e) {
			fail("Failed to convert to JSON", e);
		}

		var response = restTemplate.exchange(requestString, HttpMethod.PATCH, new HttpEntity<>(regDataJson, headers), String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		var newUserData = userRepo.findByEmail(session.getCredentialManager().getUserUser().getEmail().toLowerCase());
		assertThat(newUserData).isNotNull();
		assertThat(newUserData.getBio()).isEqualTo("testbio");
		assertThat(newUserData.getPronouns()).isNull();
	}

	@Test
	public void modifyUserInfoFull() {
		Session session = new Session(restTemplate.getRestTemplate(), serverPort);
		sessions.add(session);

		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		// Get user information pre-update
		User user = Objects.requireNonNull(userRepo.findByEmail(session.getCredentialManager().getUserUser().getEmail()));
		assertThat(user.getBio()).isNull();

		// Update user
		String requestString = "http://localhost:" + serverPort + "/api/user?apikey=" + session.getCredentialManager().getUserKey().getKey();
		UserSelfInfo updateInfo = new UserSelfInfo("testmail@mail.co", "testusername", "testbio", null);

		String regDataJson = null;

		try {
			regDataJson = new ObjectMapper().writeValueAsString(updateInfo);
		} catch (JsonProcessingException e) {
			fail("Failed to convert to JSON", e);
		}

		var response = restTemplate.exchange(requestString, HttpMethod.PUT, new HttpEntity<>(regDataJson, headers), String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		var newUserData = userRepo.findByEmail("testmail@mail.co");
		assertThat(newUserData).isNotNull();
		assertThat(newUserData.getBio()).isEqualTo("testbio");
		assertThat(newUserData.getPronouns()).isNull();
		assertThat(newUserData.getEmail()).isEqualTo("testmail@mail.co");
		assertThat(newUserData.getNonUniqueUsername()).isEqualTo("testusername");
	}
}
