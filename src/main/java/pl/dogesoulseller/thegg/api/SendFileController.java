package pl.dogesoulseller.thegg.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.api.response.FilenameResponse;
import pl.dogesoulseller.thegg.service.ImageInfoService;
import pl.dogesoulseller.thegg.service.StorageService;

@RestController
public class SendFileController {
	@Autowired
	StorageService storageService;

	@Autowired
	ImageInfoService imageInfoService;

	// TODO: Rate limiting
	@PostMapping("/api/send-file")
	@CrossOrigin
	@ResponseBody
	public ResponseEntity<FilenameResponse> sendFile(@RequestParam("file") MultipartFile file) {
		imageInfoService.getMimeExtension(file.getContentType());
		if (imageInfoService == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported MIME type " + file.getContentType());
		}

		if (file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty");
		}

		String filename;

		try {
			var fullPath = storageService.storeFile(file);
			filename = Paths.get(fullPath).getFileName().toString();
		} catch (IOException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File store failed");
		}

		return new ResponseEntity<FilenameResponse>(new FilenameResponse("Received file", filename), HttpStatus.CREATED);
	}
}
