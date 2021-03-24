package pl.dogesoulseller.thegg.inputvalidation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PasswordValidatorTests {
	@Autowired PasswordValidator passValidator;

	@Test
	void passwordRegular() {
		var pass = "thisIsAPassword";
		assertThat(passValidator.validateUserPassword(pass)).isTrue();
	}

	@Test
	void paswordRegularBad() {
		var passShort = "short";
		assertThat(passValidator.validateUserPassword(passShort)).isFalse();

		var passEmpty = "";
		assertThat(passValidator.validateUserPassword(passEmpty)).isFalse();
	}

	@Test
	void passwordAdmin() {
		var pass = "adminPassword12345.";
		assertThat(passValidator.validateAdminPassword(pass)).isTrue();
	}

	@Test
	void passwordAdminBad() {
		var passShort = "adm!nPass123";
		assertThat(passValidator.validateAdminPassword(passShort)).isFalse();

		var passNoSpecial = "adminPassword123456";
		assertThat(passValidator.validateAdminPassword(passNoSpecial)).isFalse();

		var passNoNumber = "adminPasswordWithoutNumbers.";
		assertThat(passValidator.validateAdminPassword(passNoNumber)).isFalse();

		var passNoCapital = "adminpassword.123456";
		assertThat(passValidator.validateAdminPassword(passNoCapital)).isFalse();

		var passEmpty = "";
		assertThat(passValidator.validateAdminPassword(passEmpty)).isFalse();
	}
}
