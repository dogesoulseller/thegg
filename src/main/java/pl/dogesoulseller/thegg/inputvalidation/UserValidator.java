package pl.dogesoulseller.thegg.inputvalidation;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {
	private static final Logger log = LoggerFactory.getLogger(UserValidator.class);
	
	private final Pattern emailPattern = Pattern.compile("^[a-z0-9_+&*-]+(?:\\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,7}$");
	private final Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,30}$");

	@Autowired
	public UserValidator() {
		log.info("User validator initialized");
		// TODO: Configurable verification settings
	}

	public boolean validateEmail(@NonNull String email) {
		return emailPattern.matcher(email).matches();
	}

	public boolean validateUsername(@NonNull String username) {
		return usernamePattern.matcher(username).matches();
	}
}
