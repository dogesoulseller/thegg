package pl.dogesoulseller.thegg.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import pl.dogesoulseller.thegg.user.Role;

/**
 * MongoDB repository containing defined roles
 */
public interface MongoRoleRepository extends MongoRepository<Role, String> {
	Role findByName(String name);
}
