package pl.dogesoulseller.thegg.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static pl.dogesoulseller.thegg.TestUtility.randomString;

@SpringBootTest
public class TagManagementServiceTests {
	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	public static MongoTagRepository tagRepo;
	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	public static List<String> inserted;
	@Autowired
	public TagManagementService tagService;

	@BeforeAll
	public static void initializeRepo(@Autowired MongoTagRepository tagRepoIn) {
		tagRepo = tagRepoIn;
		inserted = new ArrayList<>(3);

		List<Tag> toInsert = new ArrayList<>(3);

		for (int i = 0; i < 3; i++) {
			Tag newTag = new Tag(randomString());
			toInsert.add(newTag);
			inserted.add(newTag.getTag());
		}

		tagRepo.insert(toInsert);
	}

	@SuppressWarnings("StaticVariableUsedBeforeInitialization")
	@AfterAll
	public static void deinitializeRepo() {
		List<Tag> toDelete = new ArrayList<>(inserted.size());
		for (String tagName : inserted) {
			toDelete.add(tagRepo.findByTag(tagName));
		}

		tagRepo.deleteAll(toDelete);
	}

	@Test
	public void insertTags() {
		String preinserted = inserted.get(0);

		List<String> toInsert = new ArrayList<>(4);

		for (int i = 0; i < 3; i++) {
			toInsert.add(randomString());
		}
		inserted.addAll(toInsert);

		toInsert.add(preinserted);

		tagService.insertTags(toInsert);

		List<Tag> allTags = tagRepo.findAll();

		// Must not have created a duplicate tag
		assertThat(allTags.stream().filter((Tag t) -> t.getTag().equals(preinserted)).count()).isEqualTo(1);

		// Must have inserted all other tags exactly once
		assertThat(allTags.stream().filter((Tag t) -> toInsert.contains(t.getTag())).count()).isEqualTo(toInsert.size());
	}
}
