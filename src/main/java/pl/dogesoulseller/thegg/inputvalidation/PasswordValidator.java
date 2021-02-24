package pl.dogesoulseller.thegg.inputvalidation;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator {
	private static final Logger log = LoggerFactory.getLogger(PasswordValidator.class);
	
	// Matches passwords that are - 16 characters or longer, with at least one digit, lowercase, uppercase, and special character
	private final Pattern adminPasswordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{16,}$");

	@Autowired
	public PasswordValidator() {
		log.info("Password validator initialized");
		// TODO: Configurable verification settings
	}

	// Password for users must be 6 characters or longer
	public boolean validateUserPassword(String password) {
		return password.length() >= 6;
	}

	// Password for admins must be at least 16 characters long, with mixed digits,
	// lowercase, uppercase, special characters
	public boolean validateAdminPassword(String password) {
		return adminPasswordPattern.matcher(password).matches();
	}
}
