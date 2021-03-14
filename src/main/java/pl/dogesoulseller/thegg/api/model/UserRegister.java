package pl.dogesoulseller.thegg.api.model;

import java.lang.reflect.Field;

import org.springframework.core.style.ToStringCreator;

import lombok.*;

/**
 * Contains a new user's info received during registration
 */
@Getter
@Setter
public class UserRegister {
	private String email;
	private String username;
	private String password;
	private String passwordConfirm;

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
