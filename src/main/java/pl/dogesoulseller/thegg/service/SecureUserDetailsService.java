package pl.dogesoulseller.thegg.service;

import org.slf4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.User;

/**
 * Service handling the retrieval of user details
 */
@Service
public class SecureUserDetailsService implements UserDetailsService {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(SecureUserDetailsService.class);
	private final MongoUserRepository userRepository;

	public SecureUserDetailsService(MongoUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	// Username is set to be email, as usernames are not unique
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email);

		log.trace("Getting user details for {}", email);

		if (user == null) {
			throw new UsernameNotFoundException("User not found: " + email);
		}

		return user;
	}
}
