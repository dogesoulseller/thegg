package pl.dogesoulseller.thegg.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import pl.dogesoulseller.thegg.api.model.Post;

public interface MongoPostRepository extends MongoRepository<Post, String> {
	List<Post> findByMime(String mime);

	List<Post> findByParent(Post parent);
}
