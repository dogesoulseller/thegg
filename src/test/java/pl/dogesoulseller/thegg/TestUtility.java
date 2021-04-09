package pl.dogesoulseller.thegg;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.stream.Collectors;

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

	public static HttpHeaders basicHeaders(MediaType contentType, MediaType acceptType) {
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(List.of(acceptType));
		headers.setContentType(contentType);

		return headers;
	}

	public static HttpHeaders cookieHeaders(MediaType contentType, MediaType acceptType, String... cookies) {
		HttpHeaders header = basicHeaders(contentType, acceptType);
		header.add("Cookie", String.join("; ", cookies));

		return header;
	}
}
