package pl.dogesoulseller.thegg;

import java.nio.charset.StandardCharsets;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public class Utility {

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

	// Implementation taken from https://stackoverflow.com/a/9855338
	private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
	public static String bytesToHexString(byte[] bytes) {
		byte[] hexChars = new byte[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars, StandardCharsets.UTF_8);
	}

	public static String getServerBaseURL() {
		return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	}
}
