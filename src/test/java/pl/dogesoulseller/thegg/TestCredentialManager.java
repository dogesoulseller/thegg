package pl.dogesoulseller.thegg;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.dogesoulseller.thegg.repo.MongoKeyRepository;
import pl.dogesoulseller.thegg.repo.MongoRoleRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.ApiKey;
import pl.dogesoulseller.thegg.user.Role;
import pl.dogesoulseller.thegg.user.User;

import java.time.Instant;
import java.util.List;

import static pl.dogesoulseller.thegg.TestUtility.*;

@Configurable(autowire = Autowire.BY_TYPE)
public class TestCredentialManager implements AutoCloseable {
	private final MongoKeyRepository keyRepo;
	private final MongoUserRepository userRepo;
	private final MongoRoleRepository roleRepo;
	private final PasswordEncoder passwordEncoder;

	private Role role;
	private User userUser;
	private User adminUser;
	private ApiKey userKey;
	private ApiKey adminKey;

	@Autowired
	public TestCredentialManager() {
		keyRepo = SpringContext.getBean(MongoKeyRepository.class);
		userRepo = SpringContext.getBean(MongoUserRepository.class);
		roleRepo = SpringContext.getBean(MongoRoleRepository.class);
		passwordEncoder = SpringContext.getBean(PasswordEncoder.class);

		role = roleRepo.save(new Role("ROLE_" + randomString(), List.of("TestPrivilege")));

		userUser = userRepo.insert(new User("USER" + randomEmail(), randomUsername(), passwordEncoder.encode("testtest"), role, Instant.now()));
		adminUser = userRepo.insert(new User("ADMIN" + randomEmail(), randomUsername(), passwordEncoder.encode("testTest1234%^7890TEST"), role, Instant.now()));

		userKey = keyRepo.insert(new ApiKey(randomString(), randomString(), userUser.getId()));
		adminKey = new ApiKey(randomString(), randomString(), adminUser.getId());
		adminKey.setAdminKey(true);
		adminKey = keyRepo.insert(adminKey);
	}

	@Override
	public void close() {
		roleRepo.delete(role);
		userRepo.delete(userUser);
		userRepo.delete(adminUser);
		keyRepo.delete(userKey);
		keyRepo.delete(adminKey);
	}

	/**
	 * Get role that was created for the credential manager
	 *
	 * @return role
	 */
	public Role getRole() {
		return role;
	}

	/**
	 * Get regular user that was created for the credential manager
	 *
	 * @return regular user
	 */
	public User getUserUser() {
		return userUser;
	}

	/**
	 * Get user with admin privileges that was created for the credential manager
	 *
	 * @return admin user
	 */
	public User getAdminUser() {
		return adminUser;
	}

	/**
	 * Get API key for regular user
	 *
	 * @return user key
	 */
	public ApiKey getUserKey() {
		return userKey;
	}

	/**
	 * Get API key for administrator user
	 *
	 * @return admin key
	 */
	public ApiKey getAdminKey() {
		return adminKey;
	}
}
