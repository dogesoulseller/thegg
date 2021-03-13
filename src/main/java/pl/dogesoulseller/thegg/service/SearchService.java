package pl.dogesoulseller.thegg.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import pl.dogesoulseller.thegg.PostQueryBuilder;
import pl.dogesoulseller.thegg.PostQueryParser;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

/**
 * Service handling database searches with user queries
 */
@Service
public class SearchService {
	private static final Logger log = LoggerFactory.getLogger(SearchService.class);

	@Autowired
	private MongoPostRepository posts;

	@Autowired
	private MongoTagRepository tags;

	@Autowired
	private MongoTemplate mongoTemplate;

	/**
	 * Find paged posts using specified query and parameters
	 * @param query query
	 * @param page page to retrieve
	 * @param perPage elements per page
	 * @return page contents along with paging info
	 */
	public Page<Post> findPostsFromQuery(String query, Integer page, Integer perPage) {
		Pageable pageN = PageRequest.of(page == null ? 0 : page, perPage == null ? 30 : perPage, Sort.unsorted());
		Page<Post> foundPosts;

		if (query == null || query.isBlank()) {
			foundPosts = posts.findAll(pageN);
		} else {
			PostQueryParser qParser = new PostQueryParser(query).parse();
			PostQueryBuilder qBuilder = new PostQueryBuilder();

			qBuilder.includedTags(qParser.getInclusions());
			qBuilder.excludedTags(qParser.getExclusions());

			for (var filter : qParser.getSpecialFiltering()) {
				qBuilder.append(filter);
			}

			qBuilder.sort(qParser.getSorting());
			qBuilder.page(pageN);

			Query dbQuery = qBuilder.finish();

			log.trace("Executing post search query: {}", dbQuery.toString());

			List<Post> results = mongoTemplate.find(dbQuery, Post.class);

			foundPosts = new PageImpl<Post>(results, pageN, mongoTemplate.count(dbQuery, Page.class));
		}

		return foundPosts;
	}

	public Page<Tag> findTagFromQuery(String query, Integer page, Integer perPage) {
		Pageable pageN = PageRequest.of(page == null ? 0 : page, perPage == null ? 30 : perPage, Sort.by("tag"));
		Page<Tag> foundTags;

		if (query == null || query.isBlank()) {
			foundTags = tags.findAll(pageN);
		} else {
			String tagmatch = query.strip().split(" ")[0];
			log.trace("Executing search for tag containing: {}", tagmatch);
			foundTags = tags.findByTagContaining(tagmatch, pageN);
		}

		return foundTags;
	}
}
