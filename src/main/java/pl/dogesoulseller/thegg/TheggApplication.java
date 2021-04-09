package pl.dogesoulseller.thegg;

// import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import pl.dogesoulseller.thegg.property.PasswordValidationProperties;
import pl.dogesoulseller.thegg.property.StorageProperties;
import pl.dogesoulseller.thegg.property.UserValidationProperties;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@EnableScheduling
@EnableConfigurationProperties({
		StorageProperties.class,
		UserValidationProperties.class,
		PasswordValidationProperties.class
})
public class TheggApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheggApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

		};
	}

}