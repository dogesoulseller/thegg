package pl.dogesoulseller.thegg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import pl.dogesoulseller.thegg.Session;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.oprequest.NewOpRequest;
import pl.dogesoulseller.thegg.api.model.oprequest.OpRequest;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.repo.MongoRequestRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static pl.dogesoulseller.thegg.TestUtility.basicHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OperationRequestControllerAdminTests {
	@LocalServerPort
	int serverPort;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MongoRequestRepository requestRepo;

	@Autowired
	private MongoPostRepository postRepo;

	private static ObjectMapper mapper;
	private static JavaType resultType;

	@BeforeAll
	public static void initAll() {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		resultType = mapper.getTypeFactory().constructCollectionType(List.class, OpRequest.class);
	}

	@Test
	public void getActiveRequests() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		Session session = new Session(restTemplate, serverPort);

		requestRepo.deleteAll();

		OpRequest request0 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "", false, "NEW");

		OpRequest request1 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "", false, "NEW");

		OpRequest request2 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "", true, "REJECTED");

		List<OpRequest> requestsToRemove = requestRepo.insert(List.of(request0, request1, request2));


		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest/active?apikey=" + session.getCredentialManager().getAdminKey().getKey(),
			HttpMethod.GET, new HttpEntity<>(null, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		try {
			List<OpRequest> requests = mapper.readValue(response.getBody(), resultType);
			assertThat(requests.size()).isEqualTo(2);
		} catch (JsonProcessingException e) {
			fail("Failed to convert response from JSON");
		} finally {
			requestRepo.deleteAll(requestsToRemove);
			session.close();
		}
	}

	@Test
	public void sendReportRequest() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var insertedPost = postRepo.insert(new Post(null, null, null, null, "testname_reportreq", "safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("reportreq1"), false, null));

		Session session = new Session(restTemplate, serverPort);

		NewOpRequest opRequest = new NewOpRequest("post", "report", insertedPost.getId(), "{\"reason\": \"test reason\"}");

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(opRequest, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders().containsKey("Location")).isTrue();

		var locationHeader = Objects.requireNonNull(response.getHeaders().get("Location")).get(0);
		var reqId = locationHeader.split("id=")[1];

		assertThat(requestRepo.existsById(reqId)).isTrue();

		requestRepo.deleteAll();
		postRepo.delete(insertedPost);
		session.close();
	}

	@Test
	public void sendReportRequestTargetNotExist() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		Session session = new Session(restTemplate, serverPort);

		NewOpRequest opRequest = new NewOpRequest("post", "report", "das234tgfrews", "{\"reason\": \"test reason\"}");

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(opRequest, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		requestRepo.deleteAll();
		session.close();
	}

	@Test
	public void sendReportRequestBadPayload() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var insertedPost = postRepo.insert(new Post(null, null, null, null, "testname_reportreq_bp", "safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("reportreq1"), false, null));

		Session session = new Session(restTemplate, serverPort);

		NewOpRequest opRequest = new NewOpRequest("post", "report", insertedPost.getId(), "{\"wrong\": \"test\"}");

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(opRequest, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		requestRepo.deleteAll();
		postRepo.delete(insertedPost);
		session.close();
	}
}
