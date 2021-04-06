package pl.dogesoulseller.thegg.api;

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
import org.springframework.http.*;
import pl.dogesoulseller.thegg.Session;
import pl.dogesoulseller.thegg.api.model.NewTagInfo;
import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.api.response.PagedResults;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TagControllerTests {
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
	}

	@BeforeEach
	public void init() {
		List<Tag> tagsToSave = new ArrayList<>(7);

		tagsToSave.add(new Tag("tagconttag_tag0"));
		tagsToSave.add(new Tag("tagconttag_tag4"));
		tagsToSave.add(new Tag("tagconttag_tag1"));
		tagsToSave.add(new Tag("tagconttag_tag3"));
		tagsToSave.add(new Tag("tagconttag_tag2"));
		tagsToSave.add(new Tag("tagconttag_tag6"));
		tagsToSave.add(new Tag("tagconttag_tag7"));
		tagsToSave.add(new Tag("tagconttag_tag5"));

		tagRepository.insert(tagsToSave);
	}

	@AfterEach
	public void deinit() {
		tagRepository.deleteByTagLike("tagconttag_*");
	}

	@Test
	public void getTag() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/tag?tag=" + "tagconttag_tag1",
			HttpMethod.GET, new HttpEntity<>(null, headers), Tag.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().getDescription()).isNull();
		assertThat(response.getBody().getTag()).isEqualTo("tagconttag_tag1");
	}

	@Test
	public void getTagNotFound() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/tag?tag=" + "tagconttag_tagnoexist",
			HttpMethod.GET, new HttpEntity<>(null, headers), Tag.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	public void getTagBadParam() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/tag?tag=&",
			HttpMethod.GET, new HttpEntity<>(null, headers), Tag.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		var responseId = restTemplate.exchange("http://localhost:" + serverPort + "/api/tag?id=&",
			HttpMethod.GET, new HttpEntity<>(null, headers), Tag.class);

		assertThat(responseId.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void newTag() {
		Session session = new Session(restTemplate, serverPort);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		tagRepository.deleteByTagLike("tagcontrollertests_manualnewtag");

		NewTagInfo tagInfo = new NewTagInfo("tagcontrollertests_manualnewtag", "test description");

		var response = restTemplate.exchange("http://localhost:" + serverPort + "/api/tag?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(tagInfo, headers), String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		tagRepository.deleteByTagLike("tagcontrollertests_manualnewtag");

		session.close();
	}

	@Test
	public void newTagDuplicate() {
		Session session = new Session(restTemplate, serverPort);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		tagRepository.deleteByTagLike("tagcontrollertests_manualnewtag");

		NewTagInfo tagInfo = new NewTagInfo("tagcontrollertests_manualnewtag", "test description");

		var responseFirst = restTemplate.exchange("http://localhost:" + serverPort + "/api/tag?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(tagInfo, headers), String.class);

		assertThat(responseFirst.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		var responseSecond = restTemplate.exchange("http://localhost:" + serverPort + "/api/tag?apikey=" + session.getCredentialManager().getUserKey().getKey(), HttpMethod.POST,
			new HttpEntity<>(tagInfo, headers), String.class);

		assertThat(responseSecond.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

		tagRepository.deleteByTagLike("tagcontrollertests_manualnewtag");

		session.close();
	}
}
