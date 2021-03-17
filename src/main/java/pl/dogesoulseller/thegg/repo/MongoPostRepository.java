package pl.dogesoulseller.thegg.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import pl.dogesoulseller.thegg.api.model.Post;

/**
 * MongoDB repository containing posts
 */
public interface MongoPostRepository extends MongoRepository<Post, String> {
	List<Post> findByMime(String mime);

	List<Post> findByParent(Post parent);

	@Query("{tags:{$not:{$elemMatch:{$in:?0}}}}")
	Page<Post> filterByExcludedTags(List<String> excludedTags, Pageable pageable);
}
