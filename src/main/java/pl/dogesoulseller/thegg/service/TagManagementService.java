package pl.dogesoulseller.thegg.service;

import org.springframework.stereotype.Service;
import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service handling adding new tags from posts
 */
@Service
public class TagManagementService {
	private final MongoTagRepository tagRepo;

	public TagManagementService(MongoTagRepository tagRepo) {
		this.tagRepo = tagRepo;
	}

	/**
	 * Insert all tags, separating out the ones which are already present in the database
	 *
	 * @param tags post tags
	 */
	public void insertTags(List<String> tags) {
		List<Tag> toInsert = new ArrayList<>(tags.size());

		// Find tags that do not yet exist
		for (String tag : tags) {
			String tagTransformed = tag.toLowerCase();
			if (!tagRepo.existsByTag(tagTransformed)) {
				toInsert.add(new Tag(tagTransformed));
			}
		}

		tagRepo.insert(toInsert);
	}
}
