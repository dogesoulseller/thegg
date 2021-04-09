package pl.dogesoulseller.thegg.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.dogesoulseller.thegg.user.User;

/**
 * MongoDB repository containing user information
 */
public interface MongoUserRepository extends MongoRepository<User, String> {
	User findByEmail(String email);
}
