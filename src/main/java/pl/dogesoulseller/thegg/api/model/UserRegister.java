package pl.dogesoulseller.thegg.api.model;

import org.springframework.core.style.ToStringCreator;

import java.lang.reflect.Field;

/**
 * Contains a new user's info received during registration
 */
public class UserRegister {
	private String email;
	private String username;
	private String password;
	private String passwordConfirm;

	public UserRegister(String email, String username, String password, String passwordConfirm) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.passwordConfirm = passwordConfirm;
	}

	public UserRegister() {

	}

	public String toString() {
		var result = new ToStringCreator(this);

		for (Field field : this.getClass().getDeclaredFields()) {
			try {
				result.append(field.getName(), field.get(this));
			} catch (IllegalAccessException e) {
				result.append(field.getName(), "IllegalAccess");
			}
		}

		return result.toString();
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordConfirm() {
		return this.passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}
}
