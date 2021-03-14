package pl.dogesoulseller.thegg.inputvalidation;

import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.dogesoulseller.thegg.property.PasswordValidationProperties;

/**
 * Configurable password validation
 */
@Component
@Slf4j
public class PasswordValidator {
	private final Pattern adminPasswordPattern;
	private final Integer userPasswordMinLen;

	private final static int DEFAULT_MIN_USER_PASS_LEN = 6;
	private final static int DEFAULT_MIN_ADMIN_PASS_LEN = 16;

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
