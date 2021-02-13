package pl.dogesoulseller.thegg.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import pl.dogesoulseller.thegg.api.model.Post;

public interface MongoPostRepository extends MongoRepository<Post, String> {
	Optional<Post> findById(String id);
	List<Post> findByMime(String mime);
	List<Post> findByParent(Post parent);
}
