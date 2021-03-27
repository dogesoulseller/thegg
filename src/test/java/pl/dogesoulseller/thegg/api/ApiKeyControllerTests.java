package pl.dogesoulseller.thegg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import pl.dogesoulseller.thegg.Session;
import pl.dogesoulseller.thegg.api.model.SelfApiKeyInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoKeyRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.user.ApiKey;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.assertj.core.api.Assertions.*;

// TODO: Write tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiKeyControllerTests {
	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	private static ConcurrentLinkedDeque<Session> sessions;
	@LocalServerPort
	int serverPort;
	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private MongoKeyRepository keyRepository;
	@Autowired
	private ApiKeyVerificationService keyVerifier;

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

	@Test
	public void createKey() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", session.getSessionCookie());

		var response = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isNotBlank();

		ApiKey key = keyRepository.findByKey(response.getBody().getMessage());

		assertThat(key).isNotNull();
		assertThat(key.getKey()).isNotBlank();
		assertThat(key.getName()).isEqualTo("default");
		assertThat(key.getUserID()).isEqualTo(session.getCredentialManager().getUserUser().getId());

		keyRepository.delete(key);
	}

	@Test
	public void createKeyDuplicateName() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", session.getSessionCookie());

		var response = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey?name=testname",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getMessage()).isNotBlank();

		ApiKey key = keyRepository.findByKey(response.getBody().getMessage());

		assertThat(key).isNotNull();
		assertThat(key.getKey()).isNotBlank();
		assertThat(key.getName()).isEqualTo("testname");
		assertThat(key.getUserID()).isEqualTo(session.getCredentialManager().getUserUser().getId());

		var responseSecond = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey?name=testname",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(responseSecond.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		keyRepository.delete(key);
	}

	@Test
	public void createKeyTooManyCreated() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", session.getSessionCookie());

		for (int i = 1; i < 5; i++) {
			var response = restTemplate.postForEntity(
				"http://localhost:" + serverPort + "/api/apikey?name=testname" + i,
				new HttpEntity<>(null, headers),
				GenericResponse.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
			assertThat(response.getBody()).isNotNull();
			assertThat(response.getBody().getMessage()).isNotBlank();
		}

		var response = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey?name=testname6",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
	}

	@Test
	public void getKeyInfos() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", session.getSessionCookie());

		// Create another key
		var responseCreate = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey?name=testkey",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(responseCreate.getBody()).isNotNull();
		assertThat(responseCreate.getBody().getMessage()).isNotBlank();

		// Multi-get
		var response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/apikey",
			HttpMethod.GET, new HttpEntity<>(null, headers),
			String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();

		try {
			var mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			var collType = mapper.getTypeFactory().constructCollectionType(List.class, SelfApiKeyInfo.class);
			List<SelfApiKeyInfo> keyInfos = mapper.readValue(response.getBody(), collType);
			assertThat(keyInfos.size()).isEqualTo(2);
		} catch (JsonProcessingException e) {
			fail("Failed to parse JSON response", e);
		}
	}

	@Test
	public void getKeyInfo() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", session.getSessionCookie());

		// Create another key
		var responseCreate = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey?name=testkey",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(responseCreate.getBody()).isNotNull();
		assertThat(responseCreate.getBody().getMessage()).isNotBlank();

		// Single-key
		var response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/apikey?name=testkey",
			HttpMethod.GET, new HttpEntity<>(null, headers),
			String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();

		try {
			var mapper = new ObjectMapper();
			mapper.registerModule(new JavaTimeModule());
			var collType = mapper.getTypeFactory().constructCollectionType(List.class, SelfApiKeyInfo.class);
			List<SelfApiKeyInfo> keyInfos = mapper.readValue(response.getBody(), collType);
			assertThat(keyInfos.size()).isEqualTo(1);
			assertThat(keyInfos.get(0).getName()).isEqualTo("testkey");
		} catch (JsonProcessingException e) {
			fail("Failed to parse JSON response", e);
		}
	}

	@Test
	public void deleteKey() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", session.getSessionCookie());

		// Create a key
		var responseCreate = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey?name=testkey",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(responseCreate.getBody()).isNotNull();
		assertThat(responseCreate.getBody().getMessage()).isNotBlank();

		var response =
			restTemplate.exchange("http://localhost:" + serverPort + "/api/apikey?name=testkey",
				HttpMethod.DELETE, new HttpEntity<>(null, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void deleteKeyNotExisting() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Cookie", session.getSessionCookie());

		// Create a key
		var responseCreate = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/apikey?name=testkey1",
			new HttpEntity<>(null, headers),
			GenericResponse.class);

		assertThat(responseCreate.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(responseCreate.getBody()).isNotNull();
		assertThat(responseCreate.getBody().getMessage()).isNotBlank();

		var response =
			restTemplate.exchange("http://localhost:" + serverPort + "/api/apikey?name=testkey",
				HttpMethod.DELETE, new HttpEntity<>(null, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
