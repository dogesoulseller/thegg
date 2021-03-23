package pl.dogesoulseller.thegg;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class UtilityTests {
	@Test
	public void hexString() {
		byte[] bytesToConvert = {0x32, 0x4F, (byte) 0xA0, (byte) 0xFF, 0x00, (byte) 0xAF};
		String hexString = Utility.bytesToHexString(bytesToConvert);

		assertThat(hexString).isEqualToIgnoringCase("324FA0FF00AF");
	}

	@Test
	public void serverBaseUrl() {
		assertThat(Utility.getServerBaseURL()).contains("localhost");
	}
}
