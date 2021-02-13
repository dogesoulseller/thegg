package pl.dogesoulseller.thegg.inputvalidation;

import java.util.regex.Pattern;

public class PasswordValidator {
	// Matches passwords that are - 16 characters or longer, with at least one digit, lowercase, uppercase, and special character
	private static final Pattern adminPasswordPattern = Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{16,}$");

	// Password for users must be 6 characters or longer
	public static boolean validateUserPassword(String password) {
		return password.length() >= 6;
	}

	// Password for admins must be at least 16 characters long, with mixed digits,
	// lowercase, uppercase, special characters
	public static boolean validateAdminPassword(String password) {
		return adminPasswordPattern.matcher(password).matches();
	}
}
