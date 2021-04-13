package pl.dogesoulseller.thegg.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "validation.user")
public class UserValidationProperties {
	private Integer usernameMinLen;
	private Integer usernameMaxLen;

	public Integer getUsernameMaxLen() {
		return usernameMaxLen;
	}

	public void setUsernameMaxLen(Integer usernameMaxLen) {
		this.usernameMaxLen = usernameMaxLen;
	}

	public Integer getUsernameMinLen() {
		return usernameMinLen;
	}

	public void setUsernameMinLen(Integer usernameMinLen) {
		this.usernameMinLen = usernameMinLen;
	}
}
