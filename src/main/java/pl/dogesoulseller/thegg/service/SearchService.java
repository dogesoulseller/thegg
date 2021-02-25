package pl.dogesoulseller.thegg.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import pl.dogesoulseller.thegg.QueryParser;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

@Service
public class SearchService {
	private static final Logger log = LoggerFactory.getLogger(SearchService.class);

	@Autowired
	private MongoPostRepository posts;

	public Page<Post> findPostsFromQuery(String query, Integer page, Integer perPage) {
		Pageable pageN = PageRequest.of(page == null ? 0 : page, perPage == null ? 30 : perPage, Sort.by("creation_date").descending());
		Page<Post> foundPosts;

		if (query == null || query.isBlank()) {
			foundPosts = posts.findAll(pageN);
		} else {
			QueryParser parser = new QueryParser(query).parse();

			List<String> includedTags = parser.getIncludedTags();
			List<String> excludedTags = parser.getExcludedTags();

			if (excludedTags.isEmpty()) { // Filter by included
				foundPosts = posts.filterByTagQuery(includedTags, pageN);
			} else if (includedTags.isEmpty()) { // Filter by excluded
				foundPosts = posts.filterByExcludedTags(excludedTags, pageN);
			} else { // Filter by both
				foundPosts = posts.filterByTagQuery(includedTags, excludedTags, pageN);
			}
		}

		return foundPosts;
	}

	// TODO: User search
	// TODO: Tag search
}
