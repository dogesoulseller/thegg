package pl.dogesoulseller.thegg;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Objects;

import static pl.dogesoulseller.thegg.TestUtility.basicHeaders;

public class Session implements AutoCloseable {
	private final String sessionCookie;
	private final TestCredentialManager credentialManager;

	public Session(RestTemplate restTemplate, int serverPort) {
		credentialManager = new TestCredentialManager();

		HttpHeaders headers = basicHeaders(MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_JSON);

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("email", credentialManager.getUserUser().getEmail());
		body.add("password", "testtest");

		ResponseEntity<String> response = restTemplate.postForEntity(
			"http://localhost:" + serverPort + "/login?email=" + credentialManager.getUserUser().getEmail() + "&password=testtest",
			new HttpEntity<>(body, headers),
			String.class);

		String cookieSet = Objects.requireNonNull(response.getHeaders().get("Set-Cookie")).get(0);
		sessionCookie = Arrays.stream(cookieSet.split(";"))
			.filter((String s) -> s.contains("JSESSIONID"))
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Failed to login")).trim().replace(";", "");
	}

	/**
	 * Get string representing JSESSION cookie (like JSESSIONID=XXX...)
	 *
	 * @return cookie
	 */
	public String getSessionCookie() {
		return sessionCookie;
	}

	/**
	 * Get this session's credential manager
	 *
	 * @return TestCredentialManager
	 */
	public TestCredentialManager getCredentialManager() {
		return credentialManager;
	}

	@Override
	public void close() {
		credentialManager.close();
	}
}
