package pl.dogesoulseller.thegg.service;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import pl.dogesoulseller.thegg.repo.MongoKeyRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.ApiKey;
import pl.dogesoulseller.thegg.user.User;

import static pl.dogesoulseller.thegg.Utility.Pair;

/**
 * Service handling the verification and validation of API keys, as well as the
 * handling of user information attached to them
 */
@Service
public class ApiKeyVerificationService {
	private final MongoKeyRepository keyRepository;

	private final MongoUserRepository userRepository;

	public ApiKeyVerificationService(MongoKeyRepository keyRepository, MongoUserRepository userRepository) {
		this.keyRepository = keyRepository;
		this.userRepository = userRepository;
	}

	private Pair<Boolean, ApiKey> getKeyAndValidate(String key) {
		ApiKey foundKey = keyRepository.findByKey(key);
		Boolean valid = foundKey != null && foundKey.isActive();

		return new Pair<>(valid, foundKey);
	}

	/**
	 * Check if key is valid and not deactivated
	 *
	 * @param key key
	 * @return true if valid
	 */
	public boolean isValid(String key) {
		return getKeyAndValidate(key).getFirst();
	}

	/**
	 * Check if key is valid, not deactivated, and an admin key
	 *
	 * @param key key
	 * @return true if valid and an admin key
	 */
	public boolean isAdminValid(String key) {
		var validationResult = getKeyAndValidate(key);
		return validationResult.getFirst() && validationResult.getSecond().isAdminKey();
	}

	/**
	 * Get user attached to key, if the key is valid
	 *
	 * @param key key
	 * @return user or null
	 */
	@Nullable
	public User getKeyUser(String key) {
		var searchResult = getKeyAndValidate(key);
		if (!searchResult.getFirst()) {
			return null;
		}

		return userRepository.findById(searchResult.getSecond().getUserID()).orElse(null);
	}
}
