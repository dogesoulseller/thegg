package pl.dogesoulseller.thegg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfiguration {
	@Value("${authentication.argon.memory-pow}")
	Integer memoryPower;

	@Value("${authentication.argon.iterations}")
	Integer iterations;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Argon2PasswordEncoder(16, 32, 1, 1 << memoryPower, iterations);
	}
}
