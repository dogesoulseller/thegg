package pl.dogesoulseller.thegg.inputvalidation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class UserValidationTests {

	@Autowired UserValidator userValidator;

	@Test
	void userEmail() {
		var emailSimpleCorrect = "test@test.com";
		assertThat(userValidator.validateEmail(emailSimpleCorrect)).isTrue();

		var emailAliasedCorrect = "testing+test@test.com";
		assertThat(userValidator.validateEmail(emailAliasedCorrect)).isTrue();
	}

	@Test
	void userEmailBad() {
		var emailShort = "test@test.c";
		assertThat(userValidator.validateEmail(emailShort)).isFalse();

		var emailInvalidCharacters = "test$@t.com";
		assertThat(userValidator.validateEmail(emailInvalidCharacters)).isFalse();

		var emptyEmail = "";
		assertThat(userValidator.validateEmail(emptyEmail)).isFalse();

		var incompleteEmail = "test";
		assertThat(userValidator.validateEmail(incompleteEmail)).isFalse();
	}

	@Test
	void userUsername() {
		var username = "testname123";
		assertThat(userValidator.validateUsername(username)).isTrue();
	}

	@Test
	void userUsernameBad() {
		var usernameSpecial = "testname1@3";
		assertThat(userValidator.validateUsername(usernameSpecial)).isFalse();

		var usernameEmpty = "";
		assertThat(userValidator.validateUsername(usernameEmpty)).isFalse();
	}
}
