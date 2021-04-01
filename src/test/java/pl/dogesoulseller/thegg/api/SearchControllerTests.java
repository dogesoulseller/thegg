package pl.dogesoulseller.thegg.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.response.PagedResults;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchControllerTests {

	@Autowired
	private MongoPostRepository postRepository;

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	int serverPort;

	private static ObjectMapper mapper;
	private static JavaType pagedResultType;

	@BeforeAll
	public static void initAll() {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		pagedResultType = mapper.getTypeFactory().constructParametricType(PagedResults.class, Post.class);
	}

	@BeforeEach
	public void init() {
		List<Post> postsToSave = new ArrayList<>(7);

		postRepository.deleteByTagsContaining("searchcont_all");

		postsToSave.add(new Post(null, null, null, null, "testname_searchcont", "safe", Instant.now(), Instant.now(),
			304213, "image/png", 1280, 720,
			"authorcomm", "postcomm", List.of("searchcont_post0", "searchcont_all", "searchcont_3"), false, null));

		postsToSave.add(new Post(null, null, null, null, "testname_searchcont", "safe", Instant.now(), Instant.now(),
			564416, "image/jpeg", 1280, 720,
			"authorcomm", "postcomm", List.of("searchcont_post1", "searchcont_all", "searchcont_3"), false, null));

		postsToSave.add(new Post(null, null, null, null, "testname_searchcont", "safe", Instant.now(), Instant.now(),
			865467, "image/jpeg", 1920, 1080,
			"authorcomm", "postcomm", List.of("searchcont_post2", "searchcont_all", "searchcont_2"), false, null));

		postsToSave.add(new Post(null, null, null, null, "testname_searchcont", "safe", Instant.now(), Instant.now(),
			990485, "image/png", 3000, 5000,
			"authorcomm", "postcomm", List.of("searchcont_post3", "searchcont_all", "searchcont_2"), false, null));

		postsToSave.add(new Post(null, null, null, null, "testname_searchcont", "safe", Instant.now(), Instant.now(),
			643708, "image/jpeg", 1366, 768,
			"authorcomm", "postcomm", List.of("searchcont_post4", "searchcont_all", "searchcont_3"), false, null));

		postsToSave.add(new Post(null, null, null, null, "testname_searchcont", "safe", Instant.now(), Instant.now(),
			598592, "image/png", 400, 400,
			"authorcomm", "postcomm", List.of("searchcont_post5", "searchcont_all", "searchcont_one_first"), false, null));

		postsToSave.add(new Post(null, null, null, null, "testname_searchcont", "safe", Instant.now(), Instant.now(),
			9857324, "image/png", 5000, 5000,
			"authorcomm", "postcomm", List.of("searchcont_post6", "searchcont_all", "searchcont_one_second"), false, null));

		postRepository.insert(postsToSave);
	}

	@AfterEach
	public void deinit() {
		postRepository.deleteByTagsContaining("searchcont_all");
	}

	private PagedResults<Post> searchWithQuery(String query) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/search/post?query=" + query,
			HttpMethod.GET, new HttpEntity<>(null, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		try {
			return mapper.readValue(response.getBody(), pagedResultType);
		} catch (JsonProcessingException e) {
			fail("Failed to parse JSON response", e);
			throw new RuntimeException("Failed to parse JSON response", e);
		}
	}

	@Test
	public void findAllPosts() {
		var results = searchWithQuery("");
		assertThat(results).isNotNull();
		assertThat(results.getCurrentPage()).isEqualTo(0);
		assertThat(results.getResults()).isNotNull();
	}

	@Test
	public void findAllWithTag() {
		var results = searchWithQuery("searchcont_all");
		assertThat(results).isNotNull();
		assertThat(results.getCurrentPage()).isEqualTo(0);
		assertThat(results.getPageCount()).isEqualTo(1);
		assertThat(results.getResults()).isNotNull();

		List<Post> posts = results.getResults();
		assertThat(posts.size()).isEqualTo(7);
	}

	@Test
	public void findWithTagExcluded() {
		var results = searchWithQuery("-searchcont_3 searchcont_all");
		assertThat(results).isNotNull();
		assertThat(results.getCurrentPage()).isEqualTo(0);
		assertThat(results.getPageCount()).isEqualTo(1);
		assertThat(results.getResults()).isNotNull();

		List<Post> posts = results.getResults();
		assertThat(posts.size()).isEqualTo(4);
	}

	@Test
	public void findWithTagNoResults() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/search/post?query=tagthatdoesnotexist",
			HttpMethod.GET, new HttpEntity<>(null, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void findWithTagAdvanced() {
		var resultsGt = searchWithQuery("~size:>600000 searchcont_all");
		assertThat(resultsGt).isNotNull();
		assertThat(resultsGt.getCurrentPage()).isEqualTo(0);
		assertThat(resultsGt.getPageCount()).isEqualTo(1);
		assertThat(resultsGt.getResults()).isNotNull();
		assertThat(resultsGt.getResults().size()).isEqualTo(4);

		var resultsLt = searchWithQuery("~size:<600000 searchcont_all");
		assertThat(resultsLt).isNotNull();
		assertThat(resultsLt.getCurrentPage()).isEqualTo(0);
		assertThat(resultsLt.getPageCount()).isEqualTo(1);
		assertThat(resultsLt.getResults()).isNotNull();
		assertThat(resultsLt.getResults().size()).isEqualTo(3);

		var resultsEq = searchWithQuery("~size:=564416 searchcont_all");
		assertThat(resultsEq).isNotNull();
		assertThat(resultsEq.getCurrentPage()).isEqualTo(0);
		assertThat(resultsEq.getPageCount()).isEqualTo(1);
		assertThat(resultsEq.getResults()).isNotNull();
		assertThat(resultsEq.getResults().size()).isEqualTo(1);
	}

	@Test
	public void findWithTagAdvancedNoIneq() {
		var results = searchWithQuery("~mime:image/jpeg searchcont_all");
		assertThat(results).isNotNull();
		assertThat(results.getCurrentPage()).isEqualTo(0);
		assertThat(results.getPageCount()).isEqualTo(1);
		assertThat(results.getResults()).isNotNull();
		assertThat(results.getResults().size()).isEqualTo(3);
	}
}
