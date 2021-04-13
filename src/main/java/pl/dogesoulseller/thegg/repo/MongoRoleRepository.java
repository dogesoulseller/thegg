package pl.dogesoulseller.thegg.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import pl.dogesoulseller.thegg.user.Role;

import java.util.Optional;

/**
 * MongoDB repository containing defined roles
 */
public interface MongoRoleRepository extends MongoRepository<Role, String> {
	Optional<Role> findByName(String name);
}
