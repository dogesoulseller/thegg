package pl.dogesoulseller.thegg.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "validation.password")
public class PasswordValidationProperties {
	Integer userPasswordMinLength;
	Integer adminPasswordMinLength;

	public Integer getAdminPasswordMinLength() {
		return adminPasswordMinLength;
	}

	public void setAdminPasswordMinLength(Integer adminPasswordMinLength) {
		this.adminPasswordMinLength = adminPasswordMinLength;
	}

	public Integer getUserPasswordMinLength() {
		return userPasswordMinLength;
	}

	public void setUserPasswordMinLength(Integer userPasswordMinLength) {
		this.userPasswordMinLength = userPasswordMinLength;
	}
}
