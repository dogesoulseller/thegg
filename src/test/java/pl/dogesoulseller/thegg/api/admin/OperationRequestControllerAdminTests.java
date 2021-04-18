package pl.dogesoulseller.thegg.api.admin;

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
import pl.dogesoulseller.thegg.api.model.oprequest.OpRequest;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.repo.MongoRequestRepository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static pl.dogesoulseller.thegg.TestUtility.basicHeaders;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OperationRequestControllerAdminTests {
	private static final Pattern ID_EXTRACT_PATTERN = Pattern.compile("id=");

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
		Session session = new Session(restTemplate.getRestTemplate(), serverPort);

		OpRequest request0 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "", false, "NEW");

		OpRequest request1 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "", false, "NEW");

		OpRequest request2 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "", true, "REJECTED");

		List<OpRequest> requestsToRemove = requestRepo.insert(List.of(request0, request1, request2));

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/oprequest/admin/active?apikey=" + session.getCredentialManager().getAdminKey().getKey(),
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
	public void updateReportPut() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var insertedPost = postRepo.insert(new Post(null, null, null, null, "testname_reportreq_repput", "safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("reportreq1"), false, null));

		Session session = new Session(restTemplate.getRestTemplate(), serverPort);

		OpRequest request = new OpRequest(null, "post", "report",  session.getCredentialManager().getUserUser().getId(),
			insertedPost.getId(), Instant.now(), "", false, "NEW");

		request = requestRepo.insert(request);

		request.setPayload("{\"reason\": \"test reason for update report put\"}");

		var response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/admin/oprequest?apikey=" + session.getCredentialManager().getAdminKey().getKey(), HttpMethod.PUT,
			new HttpEntity<>(request, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		String locationHeader = Objects.requireNonNull(response.getHeaders().get("Location")).get(0);
		String requestId = ID_EXTRACT_PATTERN.split(locationHeader)[1];

		var dbRequestOpt = requestRepo.findById(requestId);
		assertThat(dbRequestOpt.isPresent()).isTrue();

		var dbRequest = dbRequestOpt.get();
		assertThat(dbRequest.getPayload()).isEqualTo("{\"reason\": \"test reason for update report put\"}");
		assertThat(dbRequest.getType()).isEqualTo("post");
		assertThat(dbRequest.getOperation()).isEqualTo("report");

		requestRepo.delete(dbRequest);
		postRepo.delete(insertedPost);
		session.close();
	}

	@Test
	public void updateReportPutInsert() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
		Session session = new Session(restTemplate.getRestTemplate(), serverPort);

		var insertedPost = postRepo.insert(new Post(null, null, null, null, "testname_reportreq_repins",
			"safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("reportreq1"), false, null));

		OpRequest request = new OpRequest(null, "post", "report", session.getCredentialManager().getUserUser().getId(),
			insertedPost.getId(), Instant.now(), "{\"reason\": \"test reason for update report put insert\"}", false, "NEW");

		var response = restTemplate.exchange(
			"http://localhost:" + serverPort + "/api/admin/oprequest?apikey=" + session.getCredentialManager().getAdminKey().getKey(), HttpMethod.PUT,
			new HttpEntity<>(request, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		String locationHeader = Objects.requireNonNull(response.getHeaders().get("Location")).get(0);
		String requestId = ID_EXTRACT_PATTERN.split(locationHeader)[1];

		var dbRequestOpt = requestRepo.findById(requestId);
		assertThat(dbRequestOpt.isPresent()).isTrue();

		var dbRequest = dbRequestOpt.get();
		assertThat(dbRequest.getPayload()).isEqualTo("{\"reason\": \"test reason for update report put insert\"}");
		assertThat(dbRequest.getType()).isEqualTo("post");
		assertThat(dbRequest.getOperation()).isEqualTo("report");

		requestRepo.delete(dbRequest);
		postRepo.delete(insertedPost);
		session.close();
	}
}
