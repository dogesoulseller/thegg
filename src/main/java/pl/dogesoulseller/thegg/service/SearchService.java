package pl.dogesoulseller.thegg.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import pl.dogesoulseller.thegg.query.PostQueryBuilder;
import pl.dogesoulseller.thegg.query.PostQueryParser;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;

import java.util.List;

/**
 * Service handling database searches with user queries
 */
@Service
public class SearchService {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SearchService.class);
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

			foundPosts = new PageImpl<>(results, pageN, mongoTemplate.count(dbQuery, Page.class));
		}

		return foundPosts;
	}

	public Page<Tag> findTagFromQuery(String query, Integer page, Integer perPage) {
		Pageable pageN = PageRequest.of(page == null ? 0 : page, perPage == null ? 30 : perPage, Sort.by(Sort.Direction.ASC, "tag"));
		Page<Tag> foundTags;

		if (query == null || query.isBlank()) {
			foundTags = tags.findAll(pageN);
		} else {
			String tagmatch = query.strip().split(" ")[0].toLowerCase();
			log.trace("Executing search for tag like: {}", tagmatch);
			foundTags = tags.findByTagLike(tagmatch, pageN);
		}

		return foundTags;
	}
}
