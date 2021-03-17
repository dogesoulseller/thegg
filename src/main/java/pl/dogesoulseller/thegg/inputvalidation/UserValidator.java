package pl.dogesoulseller.thegg.inputvalidation;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import pl.dogesoulseller.thegg.property.UserValidationProperties;

import java.util.regex.Pattern;

/**
 * Configurable user data validation
 */
@Component
public class UserValidator {
	private final static int DEFAULT_USERNAME_MIN_LEN = 3;
	private final static int DEFAULT_USERNAME_MAX_LEN = 30;
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(UserValidator.class);

	private final Pattern emailPattern = Pattern.compile("^[a-z0-9_+&*-]+(?:\\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,7}$");
	private final Pattern usernamePattern;

	@Autowired
	public UserValidator(UserValidationProperties properties) {
		Integer usernameMinLen = properties.getMinLen() == null ? DEFAULT_USERNAME_MIN_LEN : properties.getMinLen();
		Integer usernameMaxLen = properties.getMaxLen() == null ? DEFAULT_USERNAME_MAX_LEN : properties.getMaxLen();

		// Only alphanumeric with _ or -
		var usernameRegex = String.format("^[a-zA-Z0-9_-]{%d,%d}$", usernameMinLen, usernameMaxLen);
		usernamePattern = Pattern.compile(usernameRegex);

		log.info("User validator initialized");
		log.info("Username min/max len: {}/{}", usernameMinLen, usernameMaxLen);
	}

	public boolean validateEmail(@NonNull String email) {
		return emailPattern.matcher(email).matches();
	}

	public boolean validateUsername(@NonNull String username) {
		return usernamePattern.matcher(username).matches();
	}
}
