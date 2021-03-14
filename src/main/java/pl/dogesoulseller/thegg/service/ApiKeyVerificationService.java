package pl.dogesoulseller.thegg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import static pl.dogesoulseller.thegg.Utility.*;
import pl.dogesoulseller.thegg.repo.MongoKeyRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.ApiKey;
import pl.dogesoulseller.thegg.user.User;

/**
 * Service handling the verification and validation of API keys, as well as the
 * handling of user information attached to them
 */
@Service
public class ApiKeyVerificationService {
	@Autowired
	private MongoKeyRepository keyRepository;

	@Autowired
	private MongoUserRepository userRepository;

	private Pair<Boolean, ApiKey> getKeyAndValidate(String key) {
		ApiKey foundKey = keyRepository.findByKey(key);
		Boolean valid = foundKey != null && foundKey.getActive();

		return new Pair<>(valid, foundKey);
	}

	/**
	 * Check if key is valid and not deactivated
	 * @param key key
	 * @return true if valid
	 */
	public boolean isValid(String key) {
		return getKeyAndValidate(key).getFirst();
	}

	/**
	 * Get user attached to key, if the key is valid
	 * @param key key
	 * @return user or null
	 */
	public @Nullable User getKeyUser(String key) {
		var searchResult = getKeyAndValidate(key);
		if (!searchResult.getFirst()) {
			return null;
		}

		return userRepository.findById(searchResult.getSecond().getUserID()).orElse(null);
	}
}
