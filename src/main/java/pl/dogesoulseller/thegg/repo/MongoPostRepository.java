package pl.dogesoulseller.thegg.repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import pl.dogesoulseller.thegg.api.model.Post;

public interface MongoPostRepository extends MongoRepository<Post, String> {
	List<Post> findByMime(String mime);
	List<Post> findByParent(Post parent);

	@Query("{tags:{$all:?0,$not:{$elemMatch:{$in:?1}}}}")
	Page<Post> filterByTagQuery(List<String> includedTags, List<String> excludedTags, Pageable pageable);

	@Query("{tags:{$all:?0,$not:{$elemMatch:{$in:?1}}}}")
	List<Post> filterByTagQuery(List<String> includedTags, List<String> excludedTags);

	@Query("{tags:{$all:?0}}")
	Page<Post> filterByTagQuery(List<String> includedTags, Pageable pageable);

	@Query("{tags:{$all:?0}}")
	List<Post> filterByTagQuery(List<String> includedTags);

	@Query("{tags:{$not:{$elemMatch:{$in:?0}}}}")
	Page<Post> filterByExcludedTags(List<String> excludedTags, Pageable pageable);
}
