package pl.dogesoulseller.thegg.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Execution(ExecutionMode.CONCURRENT)
public class ImageInfoServiceTests {
	@Autowired ImageInfoService imageInfoService;

	@Test
	public void getImageHash() throws IOException {
		var imageStream = Objects.requireNonNull(ImageInfoServiceTests.class.getClassLoader().getResourceAsStream("testpng.png"));
		BufferedImage img = ImageIO.read(imageStream);
		assertThatNoException().isThrownBy(() -> imageInfoService.getUniqueImageHash(img));
	}
}
