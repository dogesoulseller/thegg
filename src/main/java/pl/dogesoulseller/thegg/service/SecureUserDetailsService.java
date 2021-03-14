package pl.dogesoulseller.thegg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.User;

/**
 * Service handling the retrieval of user details
 */
@Service
@Slf4j
public class SecureUserDetailsService implements UserDetailsService {
	@Autowired
	private MongoUserRepository userRepository;

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
