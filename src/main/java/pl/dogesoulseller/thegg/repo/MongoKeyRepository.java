package pl.dogesoulseller.thegg.repo;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import pl.dogesoulseller.thegg.user.ApiKey;

/**
 * MongoDB repository containing API keys
 */
public interface MongoKeyRepository extends MongoRepository<ApiKey, String> {
	public ApiKey findByNameAndUserid(String name, String userid);
	public List<ApiKey> findByUserid(String userid);
	public ApiKey findByNameAndUseridAndActive(String name, String userid, boolean active);
	public boolean existsByNameAndUserid(String name, String userid);
	public Long countByUseridAndActive(String userid, boolean active);
	public boolean existsByKeyAndActive(String key, boolean active);
	public boolean existsByKey(String key);
	public ApiKey findByKey(String key);
	public Long deleteByNameAndUserid(String name, String userid);
}
