package pl.dogesoulseller.thegg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import pl.dogesoulseller.thegg.Utility.Pair;
import pl.dogesoulseller.thegg.repo.MongoKeyRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.user.ApiKey;
import pl.dogesoulseller.thegg.user.User;

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

	public boolean isValid(String key) {
		return getKeyAndValidate(key).getFirst();
	}

	public @Nullable User getKeyUser(String key) {
		var searchResult = getKeyAndValidate(key);
		if (!searchResult.getFirst()) {
			return null;
		}

		return userRepository.findById(searchResult.getSecond().getUserID()).orElse(null);
	}
}
