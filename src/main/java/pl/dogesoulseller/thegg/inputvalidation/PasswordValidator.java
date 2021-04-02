package pl.dogesoulseller.thegg.inputvalidation;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.dogesoulseller.thegg.property.PasswordValidationProperties;

import java.util.regex.Pattern;

/**
 * Configurable password validation
 */
@Component
public class PasswordValidator {
	private static final int DEFAULT_MIN_USER_PASS_LEN = 6;
	private static final int DEFAULT_MIN_ADMIN_PASS_LEN = 16;
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(PasswordValidator.class);
	private final Pattern adminPasswordPattern;
	private final Integer userPasswordMinLen;

	@Autowired
	public PasswordValidator(PasswordValidationProperties properties) {
		Integer adminPasswordMinLen = properties.getAdminPasswordMinLength() == null ? DEFAULT_MIN_ADMIN_PASS_LEN
				                              : properties.getAdminPasswordMinLength();

		userPasswordMinLen = properties.getUserPasswordMinLength() == null ? DEFAULT_MIN_USER_PASS_LEN
				                     : properties.getUserPasswordMinLength();

		// Matches passwords that are - X characters or longer, with at least one digit,
		// lowercase, uppercase, and special character
		var adminPattern = String.format("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{%d,}$",
				adminPasswordMinLen);

		adminPasswordPattern = Pattern.compile(adminPattern);

		log.info("Password validator initialized");
		log.info("User password min/max len: {}/{}", userPasswordMinLen, "None");
		log.info("Admin password min/max len: {}/{}", adminPasswordMinLen, "None");
	}

	public boolean validateUserPassword(String password) {
		return password.length() >= userPasswordMinLen;
	}

	public boolean validateAdminPassword(String password) {
		return adminPasswordPattern.matcher(password).matches();
	}
}
