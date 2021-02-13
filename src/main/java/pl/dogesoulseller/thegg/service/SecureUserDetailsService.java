package pl.dogesoulseller.thegg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.User;

@Service
public class SecureUserDetailsService implements UserDetailsService {

	@Autowired
	private MongoUserRepository userRepository;

	// Username is set to be email, as usernames are not unique
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email);

		if (user == null) {
			throw new UsernameNotFoundException("User not found");
		}

		return user;
	}
}
