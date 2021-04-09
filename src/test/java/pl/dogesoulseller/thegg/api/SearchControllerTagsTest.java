package pl.dogesoulseller.thegg.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static pl.dogesoulseller.thegg.TestUtility.basicHeaders;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.api.response.PagedResults;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SearchControllerTagsTest {
	@LocalServerPort
	int serverPort;

	@Autowired
	private MongoTagRepository tagRepository;

	@Autowired
	private TestRestTemplate restTemplate;

	private static ObjectMapper mapper;
	private static JavaType pagedResultType;

	@BeforeAll
	public static void initAll() {
		mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		pagedResultType = mapper.getTypeFactory().constructParametricType(PagedResults.class, Tag.class);
	}

	@BeforeEach
	public void init() {
		List<Tag> tagsToSave = new ArrayList<>(7);

		tagsToSave.add(new Tag("searchconttag_tag0"));
		tagsToSave.add(new Tag("searchconttag_tag4"));
		tagsToSave.add(new Tag("searchconttag_tag1"));
		tagsToSave.add(new Tag("searchconttag_tag3"));
		tagsToSave.add(new Tag("searchconttag_tag2"));
		tagsToSave.add(new Tag("searchconttag_othertagtype5"));
		tagsToSave.add(new Tag("searchconttag_othertagtype6"));
		tagsToSave.add(new Tag("searchconttag_othertagtype7"));

		tagRepository.insert(tagsToSave);
	}

	@AfterEach
	public void deinit() {
		tagRepository.deleteByTagLike("searchconttag_*");
	}

	private PagedResults<Tag> searchWithQuery(String query) {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/search/tag?query=" + query,
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
	public void getAllTags() {
		var results = searchWithQuery("");
		assertThat(results).isNotNull();
		assertThat(results.getCurrentPage()).isEqualTo(0);
		assertThat(results.getResults()).isNotNull();
	}

	@Test
	public void getSpecificTag() {
		var results = searchWithQuery("searchconttag_tag0");
		assertThat(results).isNotNull();
		assertThat(results.getCurrentPage()).isEqualTo(0);
		assertThat(results.getResults()).isNotNull();
		assertThat(results.getResults().size()).isEqualTo(1);
	}

	@Test
	void getMultiTag() {
		var results = searchWithQuery("searchconttag_t");
		assertThat(results).isNotNull();
		assertThat(results.getCurrentPage()).isEqualTo(0);
		assertThat(results.getResults()).isNotNull();
		assertThat(results.getResults().size()).isEqualTo(5);

		for (int i = 0; i < 5; i++) {
			assertThat(results.getResults().get(i).getTag()).endsWith(Integer.toString(i));
		}
	}

	@Test
	void getTagNoResults() {
		HttpHeaders headers = basicHeaders(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/search/tag?query=" + "tagnoresults",
			HttpMethod.GET, new HttpEntity<>(null, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}
