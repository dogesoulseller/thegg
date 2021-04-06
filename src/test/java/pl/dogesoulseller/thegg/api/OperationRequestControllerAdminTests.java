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
import pl.dogesoulseller.thegg.api.model.OpRequest;
import pl.dogesoulseller.thegg.repo.MongoRequestRepository;

import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OperationRequestControllerAdminTests {
	@LocalServerPort
	int serverPort;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private MongoRequestRepository requestRepo;

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
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		Session session = new Session(restTemplate, serverPort);

		requestRepo.deleteAll();

		OpRequest request0 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "Test Reason", false, "NEW");

		OpRequest request1 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "Test Reason 1", false, "NEW");

		OpRequest request2 = new OpRequest(null, "USER", "REPORT", session.getCredentialManager().getUserUser().getId(),
			session.getCredentialManager().getAdminUser().getId(), Instant.now(), "Test Reason 2", true, "REJECTED");

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
}
