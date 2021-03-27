package pl.dogesoulseller.thegg.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.dogesoulseller.thegg.TestCredentialManager;
import pl.dogesoulseller.thegg.user.User;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class ApiKeyVerificationServiceTests {
	@Autowired
	ApiKeyVerificationService keyService;

	@SuppressWarnings("StaticVariableMayNotBeInitialized")
	public static TestCredentialManager credentialManager;

	@BeforeAll
	public static void initializeRepos() {
		credentialManager = new TestCredentialManager();
	}

	@AfterAll
	public static void deinitializeRepos() {
		//noinspection StaticVariableUsedBeforeInitialization
		credentialManager.close();
	}

	@Test
	public void getUserFromKey() {
		User regularUser = keyService.getKeyUser(credentialManager.getUserKey().getKey());
		User adminUser = keyService.getKeyUser(credentialManager.getAdminKey().getKey());

		assertThat(regularUser).isNotNull();
		assertThat(adminUser).isNotNull();

		assertThat(regularUser.getEmail()).contains("USER");
		assertThat(adminUser.getEmail()).contains("ADMIN");
	}

	@Test
	public void validateUserKey() {
		assertThat(keyService.isValid(credentialManager.getUserKey().getKey())).isTrue();
		assertThat(keyService.isValid(credentialManager.getAdminKey().getKey())).isTrue();
	}

	@Test
	public void validateAdminKey() {
		assertThat(keyService.isAdminValid(credentialManager.getUserKey().getKey())).isFalse();
		assertThat(keyService.isAdminValid(credentialManager.getAdminKey().getKey())).isTrue();
	}
}
