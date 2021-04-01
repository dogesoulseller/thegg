package pl.dogesoulseller.thegg.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import pl.dogesoulseller.thegg.api.model.Tag;

/**
 * MongoDB repository containing defined post tags
 */
public interface MongoTagRepository extends MongoRepository<Tag, String> {
	Tag findByTag(String tag);
	boolean existsByTag(String tag);
	Page<Tag> findByTagContaining(String tagLike, Pageable pageable);
	void deleteByTagLike(String tagLike);

}
