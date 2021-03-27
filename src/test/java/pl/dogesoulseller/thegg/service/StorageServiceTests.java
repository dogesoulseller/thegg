package pl.dogesoulseller.thegg.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class StorageServiceTests {
	public MultipartFile mpFile;
	@Autowired StorageService storageService;

	@BeforeEach
	public void initMultipart() {
		try {
			mpFile = new MockMultipartFile("testFile.png", "testFile.png", "image/png",
					StorageServiceTests.class.getClassLoader().getResourceAsStream("testpng.png"));

			if (mpFile.isEmpty()) {
				throw new RuntimeException("Failed to initialize mock multipart file");
			}
		} catch (IOException e) {
			fail("Failed to initialize mock multipart file", e);
		}
	}

	@Test
	public void storeMultipartFile() {
		try {
			String name = storageService.storeFile(mpFile);

			assertThat(storageService.getFromTempStorage(name).exists()).isTrue();
			assertThat(storageService.getFromPermanentStorage(Path.of(name).getFileName().toString())).doesNotExist();
		} catch (IOException e) {
			fail(e.toString());
		}
	}

	@Test
	public void storeToPermanentStorage() {
		try {
			File tempFile = storageService.getFromTempStorage(Path.of(storageService.storeFile(mpFile)).getFileName().toString());

			Path outputPath = Path.of(storageService.storeFileToPermanentStorage(tempFile, "testfile.png"));

			assertThat(storageService.getFromPermanentStorage(outputPath.getFileName().toString())).exists();

			Files.delete(outputPath);
		} catch (IOException e) {
			fail(e.toString());
		}
	}
}
