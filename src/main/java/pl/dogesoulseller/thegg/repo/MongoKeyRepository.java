package pl.dogesoulseller.thegg.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import pl.dogesoulseller.thegg.user.ApiKey;

public interface MongoKeyRepository extends MongoRepository<ApiKey, String> {
	public ApiKey findByNameAndUserid(String name, String userid);
	public boolean existsByNameAndUserid(String name, String userid);
	public Long countByUseridAndActive(String userid, boolean active);
	public boolean existsByKeyAndActive(String key, boolean active);
	public boolean existsByKey(String key);
	public ApiKey findByKey(String key);
	public Long deleteByNameAndUserid(String name, String userid);
}
