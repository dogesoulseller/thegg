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
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.PostInfo;
import pl.dogesoulseller.thegg.api.response.FilenameResponse;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

// TODO: Write tests
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostControllerTests {
	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	private static ConcurrentLinkedDeque<Session> sessions;
	@LocalServerPort
	int serverPort;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MongoPostRepository postRepository;

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

	private String uploadTestFile(Session session, String filename) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

		try {
			File file = new ClassPathResource(filename).getFile();
			FileSystemResource resource = new FileSystemResource(file);
			body.add("file", resource);
		} catch (IOException e) {
			fail("Failed to create file resource", e);
		}

		ResponseEntity<FilenameResponse> createFileResponse = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/send-file?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(body, headers),
			FilenameResponse.class);

		assertThat(createFileResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(createFileResponse.getBody()).isNotNull();

		var createdFileName = createFileResponse.getBody().getFilename();
		assertThat(createdFileName).isNotEmpty();

		return createdFileName;
	}

	@Test
	public void createPost() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		String filename = uploadTestFile(session, "testpng.png");

		PostInfo info = new PostInfo(filename, "safe", null, null, "test", "testposter", List.of("testcreate01", "testcreate02"));

		ResponseEntity<GenericResponse> response = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(info, headers), GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getBody()).isNotNull();

		var locationHeader = Objects.requireNonNull(response.getHeaders().get("Location")).get(0);
		var postId = locationHeader.split("id=")[1];

		Optional<Post> createdPost = postRepository.findById(postId);
		if (createdPost.isEmpty()) {
			fail("Failed to get created post from repository");
			return; // Workaround for false positive IDE warning
		}

		assertThat(createdPost.get().getRating()).isEqualTo("safe");
		assertThat(createdPost.get().getAuthorComment()).isEqualTo("test");
		assertThat(createdPost.get().getPosterComment()).isEqualTo("testposter");
		assertThat(createdPost.get().getTags().size()).isEqualTo(2);

		postRepository.delete(createdPost.get());
	}

	@Test
	public void createPostNoFile() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		PostInfo info = new PostInfo("nonexistent.png", "safe", null, null, "test", "testposter", List.of("testcreate01", "testcreate02"));

		ResponseEntity<GenericResponse> response = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(info, headers), GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void createPostRatingInvalid() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		String filename = uploadTestFile(session, "testpng.png");

		PostInfo info = new PostInfo(filename, "invalid", null, null, "test", "testposter", List.of("testcreate01", "testcreate02"));

		ResponseEntity<GenericResponse> response = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(info, headers), GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void getPost() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		String filename = uploadTestFile(session, "testpng.png");

		PostInfo info = new PostInfo(filename, "safe", null, null, "test", "testposter", List.of("testcreate01", "testcreate02"));

		ResponseEntity<GenericResponse> sendResponse = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(info, headers), GenericResponse.class);

		assertThat(sendResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var locationHeader = Objects.requireNonNull(sendResponse.getHeaders().get("Location")).get(0);
		var postId = locationHeader.split("id=")[1];

		ResponseEntity<Post> response = restTemplate.exchange("http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey()
			+ "&id=" + postId, HttpMethod.GET, new HttpEntity<>(null, headers), Post.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();

		assertThat(response.getBody().getAuthorComment()).isEqualTo("test");
		assertThat(response.getBody().getPosterComment()).isEqualTo("testposter");
		assertThat(response.getBody().getTags().size()).isEqualTo(2);
		assertThat(response.getBody().getTags()).contains("testcreate01", "testcreate02");
		assertThat(response.getBody().getId()).isEqualTo(postId);
	}

	@Test
	public void getPostNonexistent() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		ResponseEntity<Post> response = restTemplate.exchange("http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey()
			+ "&id=10", HttpMethod.GET, new HttpEntity<>(null, headers), Post.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void deletePost() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		String filename = uploadTestFile(session, "testpng.png");

		PostInfo info = new PostInfo(filename, "safe", null, null, "test", "testposter", List.of("testcreate01", "testcreate02"));

		ResponseEntity<GenericResponse> sendResponse = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(info, headers), GenericResponse.class);

		assertThat(sendResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var locationHeader = Objects.requireNonNull(sendResponse.getHeaders().get("Location")).get(0);
		var postId = locationHeader.split("id=")[1];

		assertThat(postRepository.existsById(postId)).isTrue();

		ResponseEntity<GenericResponse> response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey() + "&id=" + postId,
			HttpMethod.DELETE, new HttpEntity<>(null, headers), GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(postRepository.existsById(postId)).isFalse();
	}

	@Test
	public void deletePostNonexistent() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		ResponseEntity<GenericResponse> response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey() + "&id=10",
			HttpMethod.DELETE, new HttpEntity<>(null, headers), GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void modifyPost() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		String filename = uploadTestFile(session, "testpng.png");

		PostInfo info = new PostInfo(filename, "safe", null, null, "test", "testposter", List.of("testcreate01", "testcreate02"));

		ResponseEntity<GenericResponse> sendResponse = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(info, headers), GenericResponse.class);

		assertThat(sendResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var locationHeader = Objects.requireNonNull(sendResponse.getHeaders().get("Location")).get(0);
		var postId = locationHeader.split("id=")[1];

		PostInfo updateInfo = new PostInfo(null, null, null, null, "newtest", null, null);

		ResponseEntity<GenericResponse> response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey() + "&id=" + postId,
			HttpMethod.PATCH, new HttpEntity<>(updateInfo, headers), GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		var newPostOptional = postRepository.findById(postId);
		assertThat(newPostOptional).isNotEmpty();

		Post post = newPostOptional.get();
		assertThat(post.getAuthorComment()).isEqualTo("newtest");
	}

	@Test
	public void modifyPostPut() {
		Session session = new Session(restTemplate, serverPort);
		sessions.add(session);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		String filename = uploadTestFile(session, "testpng.png");

		PostInfo info = new PostInfo(filename, "safe", null, null, "test", "testposter", List.of("testcreate01", "testcreate02"));

		ResponseEntity<GenericResponse> sendResponse = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey(),
			new HttpEntity<>(info, headers), GenericResponse.class);

		assertThat(sendResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var locationHeader = Objects.requireNonNull(sendResponse.getHeaders().get("Location")).get(0);
		var postId = locationHeader.split("id=")[1];

		PostInfo updateInfo = new PostInfo(null, "questionable", null, null, "newtest", null, List.of("example"));

		ResponseEntity<GenericResponse> response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/post?apikey=" + session.getCredentialManager().getUserKey().getKey() + "&id=" + postId,
			HttpMethod.PUT, new HttpEntity<>(updateInfo, headers), GenericResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		var newPostOptional = postRepository.findById(postId);
		assertThat(newPostOptional).isNotEmpty();

		Post post = newPostOptional.get();
		assertThat(post.getAuthorComment()).isEqualTo("newtest");
		assertThat(post.getPosterComment()).isNull();
		assertThat(post.getParent()).isNull();
	}
}
