package pl.dogesoulseller.thegg.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import pl.dogesoulseller.thegg.user.ApiKey;

/**
 * MongoDB repository containing API keys
 */
public interface MongoKeyRepository extends MongoRepository<ApiKey, String> {
	ApiKey findByNameAndUserid(String name, String userid);

	List<ApiKey> findByUserid(String userid);

	ApiKey findByNameAndUseridAndActive(String name, String userid, boolean active);

	boolean existsByNameAndUserid(String name, String userid);

	Long countByUseridAndActive(String userid, boolean active);

	boolean existsByKeyAndActive(String key, boolean active);

	boolean existsByKey(String key);

	ApiKey findByKey(String key);

	Long deleteByNameAndUserid(String name, String userid);
}
