package pl.dogesoulseller.thegg;

import java.util.UUID;

public class TestUtility {
	public static String randomString() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public static String randomUsername() {
		return randomString().toLowerCase();
	}

	public static String randomEmail() {
		return UUID.randomUUID().toString().replace("-", "") + "@doge.com";
	}
}
