package pl.dogesoulseller.thegg.property;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "validation.user")
public class UserValidationProperties {
	Integer minLen;
	Integer maxLen;

	public Integer getMaxLen() {
		return maxLen;
	}

	public Integer getMinLen() {
		return minLen;
	}

	public void setMaxLen(Integer maxLen) {
		this.maxLen = maxLen;
	}

	public void setMinLen(Integer minLen) {
		this.minLen = minLen;
	}
}
