package pl.dogesoulseller.thegg.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pl.dogesoulseller.thegg.property.StorageProperties;

@Service
public class StorageService {
	private static final Logger log = LoggerFactory.getLogger(StorageService.class);

	private Path tempStoragePath;
	private Path storagePath;

	@Autowired
	public StorageService(StorageProperties properties) throws IOException {
		this.tempStoragePath = Paths.get(properties.getFileUploadDestination()).toAbsolutePath().normalize();
		this.storagePath = Paths.get(properties.getImageStorageDir()).toAbsolutePath().normalize();

		Files.createDirectories(tempStoragePath);
		Files.createDirectories(storagePath);

		log.info("Storage service initialized");
	}

	public String storeFile(MultipartFile file) throws IOException {
		Path output = tempStoragePath.resolve(UUID.randomUUID().toString());
		Files.copy(file.getInputStream(), output, StandardCopyOption.REPLACE_EXISTING);

		log.debug("Stored temp file of size %d as %s", file.getSize(), output.toString());

		return output.toString();
	}

	public String storeFileToPermanentStorage(File file, String newName) throws IOException {
		Path output = storagePath.resolve(newName);
		Files.move(Paths.get(file.getAbsolutePath()), output, StandardCopyOption.REPLACE_EXISTING);

		log.debug("Stored permanent file of size %d as %s", file.length(), output.toString());

		return output.toString();
	}

	public File getFromTempStorage(String name) {
		return new File(tempStoragePath.resolve(name).toString());
	}

	public File getFromPermanentStorage(String name) {
		return new File(storagePath.resolve(name).toString());
	}

	// TODO: Run every 10 minutes
	@Scheduled(fixedDelay = 600000)
	public void cleanOldTempFiles() {
		log.info("Deleting files...");
		try {
			Files.walkFileTree(tempStoragePath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					var fileTime = attrs.creationTime().toInstant();
					var currentTime = Instant.now();

					// Files are stored for a max of 10 minutes
					// TODO: Figure out a sane time
					var timeBetween = Math.abs(Duration.between(fileTime, currentTime).toMinutes());
					if (timeBetween > 10) {
						Files.deleteIfExists(file);
					}

					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.info("Files deleted");
	}
}
