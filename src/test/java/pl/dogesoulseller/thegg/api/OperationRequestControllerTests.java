package pl.dogesoulseller.thegg.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import pl.dogesoulseller.thegg.Session;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.oprequest.NewOpRequest;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.repo.MongoRequestRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.dogesoulseller.thegg.TestUtility.basicHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OperationRequestControllerTests {
	private static final Pattern ID_EXTRACT_PATTERN = Pattern.compile("id=");

	@LocalServerPort
	int serverPort;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MongoRequestRepository requestRepo;

	@Autowired
	private MongoPostRepository postRepo;

	@Test
	public void sendReportRequest() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var insertedPost = postRepo.insert(new Post(null, null, null, null, "testname_reportreq", "safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("reportreq1"), false, null));

		Session session = new Session(restTemplate.getRestTemplate(), serverPort);

		NewOpRequest opRequest = new NewOpRequest("post", "report", insertedPost.getId(), "{\"reason\": \"test reason\"}");

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(opRequest, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().containsKey("Location")).isTrue();

		var locationHeader = Objects.requireNonNull(response.getHeaders().get("Location")).get(0);
		var reqId = ID_EXTRACT_PATTERN.split(locationHeader)[1];

		assertThat(requestRepo.existsById(reqId)).isTrue();

		requestRepo.deleteById(reqId);
		postRepo.delete(insertedPost);
		session.close();
	}

	@Test
	public void sendReportRequestTargetNotExist() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		Session session = new Session(restTemplate.getRestTemplate(), serverPort);

		NewOpRequest opRequest = new NewOpRequest("post", "report", "das234tgfrews", "{\"reason\": \"test reason\"}");

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(opRequest, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		session.close();
	}

	@Test
	public void sendReportRequestBadPayload() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var insertedPost = postRepo.insert(new Post(null, null, null, null, "testname_reportreq_bp", "safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("reportreq1"), false, null));

		Session session = new Session(restTemplate.getRestTemplate(), serverPort);

		NewOpRequest opRequest = new NewOpRequest("post", "report", insertedPost.getId(), "{\"wrong\": \"test\"}");

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(opRequest, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		postRepo.delete(insertedPost);
		session.close();
	}

	@Test
	public void cancelReport() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var insertedPost = postRepo.insert(new Post(null, null, null, null, "testname_reportreq", "safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("reportreq1"), false, null));

		Session session = new Session(restTemplate.getRestTemplate(), serverPort);

		NewOpRequest opRequest = new NewOpRequest("post", "report", insertedPost.getId(), "{\"reason\": \"test reason cancel\"}");

		var responsePost = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(opRequest, headers), String.class);

		var locationHeader = Objects.requireNonNull(responsePost.getHeaders().get("Location")).get(0);
		var reqId = ID_EXTRACT_PATTERN.split(locationHeader)[1];

		assertThat(requestRepo.existsById(reqId)).isTrue();

		var response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey() + "&id=" + reqId,
			HttpMethod.DELETE,  new HttpEntity<>(null, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		var dbRequest = requestRepo.findById(reqId);
		assertThat(dbRequest.isPresent()).isTrue();

		assertThat(dbRequest.get().getResolved()).isTrue();
		assertThat(dbRequest.get().getStatus()).isEqualTo("USER CANCELLED");
	}
}
