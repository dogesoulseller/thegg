package pl.dogesoulseller.thegg.api.model;

import java.lang.reflect.Field;

import org.springframework.core.style.ToStringCreator;

/**
 * Contains a new user's info received during registration
 */
public class UserRegister {
	private String email;
	private String username;
	private String password;
	private String passwordConfirm;

	public String getEmail() {
		return email;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public String toString() {
		var result = new ToStringCreator(this);

		for (Field field: this.getClass().getDeclaredFields()) {
			try {
				result.append(field.getName(), field.get(this));
			} catch (IllegalAccessException e) {
				result.append(field.getName(), "IllegalAccess");
			}
		}

		return result.toString();
	}
}
