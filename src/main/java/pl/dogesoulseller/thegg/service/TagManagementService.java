package pl.dogesoulseller.thegg.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

@Service
public class TagManagementService {
	@Autowired
	MongoTagRepository tagRepo;

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
