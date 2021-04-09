package pl.dogesoulseller.thegg;

import java.nio.charset.StandardCharsets;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Miscallaneous utilities
 */
public class Utility {

	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

	/**
	 * Represent byte array as a hexadecimal string. Implementation taken from https://stackoverflow.com/a/9855338
	 *
	 * @param bytes byte array
	 * @return string representation of hex digits
	 */
	public static String bytesToHexString(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	/**
	 * Get base URL of current server, for example http://localhost:8080
	 *
	 * @return base URL
	 */
	public static String getServerBaseURL() {
		return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	}

	/**
	 * Pair of any two objects
	 */
	public static class Pair<T, U> {
		T first;
		U second;

		public Pair(T first, U second) {
			this.first = first;
			this.second = second;
		}

		public T getFirst() {
			return first;
		}

		public U getSecond() {
			return second;
		}
	}
}
