package pl.dogesoulseller.thegg.api;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import pl.dogesoulseller.thegg.Session;
import pl.dogesoulseller.thegg.TestCredentialManager;
import pl.dogesoulseller.thegg.api.response.FilenameResponse;
import pl.dogesoulseller.thegg.service.StorageService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.assertj.core.api.Assertions.*;
import static pl.dogesoulseller.thegg.TestUtility.randomString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SendFileControllerTests {
	@LocalServerPort
	int serverPort;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private StorageService storageService;

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

	@Test
	public void sendImageFile() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		try {
			File file = new ClassPathResource("testpng.png").getFile();
			FileSystemResource resource = new FileSystemResource(file);
			body.add("file", resource);
		} catch (IOException e) {
			fail("Failed to create file resource", e);
		}

		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		ResponseEntity<FilenameResponse> response = restTemplate.postForEntity(
				"http://localhost:" + serverPort + "/api/send-file?apikey=" + session.getCredentialManager().getUserKey().getKey(),
				new HttpEntity<>(body, headers),
				FilenameResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();

		var createdFileName = response.getBody().getFilename();
		assertThat(createdFileName).isNotEmpty();

		storageService.getFromTempStorage(createdFileName);
	}

	@Test
	public void sendFileEmpty() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		try {
			File file = File.createTempFile(randomString(), null);
			FileSystemResource resource = new FileSystemResource(file);
			body.add("file", resource);
		} catch (IOException e) {
			fail("Failed to create file resource", e);
		}

		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		ResponseEntity<FilenameResponse> response = restTemplate.postForEntity(
				"http://localhost:" + serverPort + "/api/send-file?apikey=" + session.getCredentialManager().getUserKey().getKey(),
				new HttpEntity<>(body, headers),
				FilenameResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void sendFileUnsupportedMime() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		try {
			File file = new ClassPathResource("testtext.txt").getFile();
			FileSystemResource resource = new FileSystemResource(file);
			body.add("file", resource);
		} catch (IOException e) {
			fail("Failed to create file resource", e);
		}

		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

			ResponseEntity<FilenameResponse> response = restTemplate.postForEntity(
					"http://localhost:" + serverPort + "/api/send-file?apikey=" + session.getCredentialManager().getUserKey().getKey(),
					new HttpEntity<>(body, headers),
					FilenameResponse.class);

			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}
}
