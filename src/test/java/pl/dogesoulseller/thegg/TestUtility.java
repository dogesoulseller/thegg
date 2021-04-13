package pl.dogesoulseller.thegg;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

public class TestUtility {
	/**
	 * Generate a random string from a UUID with - stripped
	 *
	 * @return random string
	 */
	public static String randomString() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * Generate a random username from a UUID with - stripped and converted to lowercase
	 *
	 * @return random username
	 */
	public static String randomUsername() {
		return randomString().toLowerCase();
	}

	/**
	 * Generate a random username from a UUID with - stripped and converted to lowercase, with @doge.com appended
	 *
	 * @return random email
	 */
	public static String randomEmail() {
		return UUID.randomUUID().toString().toLowerCase().replace("-", "") + "@doge.com";
	}

	/**
	 * Create HttpHeaders object with Content-Type and Accept headers
	 *
	 * @param contentType Content-Type
	 * @param acceptType  Accept
	 * @return generated HttpHeaders
	 */
	public static HttpHeaders basicHeaders(MediaType contentType, MediaType acceptType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(acceptType));
		headers.setContentType(contentType);

		return headers;
	}

	/**
	 * Create HttpHeaders object with Content-Type and Accept headers, with specified cookies appended
	 *
	 * @param contentType Content-Type
	 * @param acceptType  Accept
	 * @param cookies     cookies
	 * @return generated HttpHeaders
	 */
	public static HttpHeaders cookieHeaders(MediaType contentType, MediaType acceptType, String... cookies) {
		HttpHeaders header = basicHeaders(contentType, acceptType);
		header.add("Cookie", String.join("; ", cookies));

		return header;
	}
}
