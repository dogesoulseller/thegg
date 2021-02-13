package pl.dogesoulseller.thegg.inputvalidation;

import java.util.regex.Pattern;

import org.springframework.lang.NonNull;

public class UserValidator {
	private static final Pattern emailPattern = Pattern.compile("^[a-z0-9_+&*-]+(?:\\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,7}$");
	private static final Pattern usernamePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,30}$");

	public static boolean validateEmail(@NonNull String email) {
		return emailPattern.matcher(email).matches();
	}

	public static boolean validateUsername(@NonNull String username) {
		return usernamePattern.matcher(username).matches();
	}
}
