package pl.dogesoulseller.thegg.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.Utility;
import pl.dogesoulseller.thegg.api.response.FilenameResponse;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.service.ImageInfoService;
import pl.dogesoulseller.thegg.service.StorageService;

@RestController
public class SendFileController {
	private static final Logger log = LoggerFactory.getLogger(SendFileController.class);

	@Autowired
	StorageService storageService;

	@Autowired
	ImageInfoService imageInfoService;

	@Autowired
	private ApiKeyVerificationService keyVerifier;

	// TODO: Rate limiting
	@PostMapping("/api/send-file")
	@CrossOrigin
	public ResponseEntity<FilenameResponse> sendFile(@RequestParam String apikey, @RequestParam("file") MultipartFile file) {
		var user = keyVerifier.getKeyUser(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		String mimeExtension = imageInfoService.getMimeExtension(file.getContentType());
		if (mimeExtension == null) {
			log.error("Trying to receive unsupported MIME type {}", file.getContentType());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported MIME type " + file.getContentType());
		}

		if (file.isEmpty()) {
			log.error("Trying to receive empty file");
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
		}

		String filename;

		try {
			var fullPath = storageService.storeFile(file);
			filename = Paths.get(fullPath).getFileName().toString();
		} catch (IOException e) {
			log.error("Could not store file {}", file.getName());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File store failed");
		}

		log.info("Received file {}", filename);
		return new ResponseEntity<FilenameResponse>(new FilenameResponse("Received file", filename, Utility.getServerBaseURL() + "/api/post"), HttpStatus.CREATED);
	}
}
