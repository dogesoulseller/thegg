package pl.dogesoulseller.thegg.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

/**
 * Service handling adding new tags from posts
 */
@Service
public class TagManagementService {
	@Autowired
	MongoTagRepository tagRepo;

	/**
	 * Insert all tags, separating out the ones which are already present in the database
	 * @param tags post tags
	 */
	public void insertTags(List<String> tags) {
		List<Tag> toInsert = new ArrayList<>(tags.size());

		// Find tags that do not yet exist
		for (String tag : tags) {
			if (!tagRepo.existsByTag(tag)) {
				toInsert.add(new Tag(tag));
			}
		}

		tagRepo.insert(toInsert);
	}
}
