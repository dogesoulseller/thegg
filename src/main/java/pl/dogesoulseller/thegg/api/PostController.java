package pl.dogesoulseller.thegg.api;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.PostInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.repo.MongoUserRepository;
import pl.dogesoulseller.thegg.service.ImageInfoService;
import pl.dogesoulseller.thegg.service.StorageService;
import pl.dogesoulseller.thegg.user.User;

import org.springframework.web.bind.annotation.PostMapping;

@RestController
public class PostController {
	@Autowired
	private MongoPostRepository posts;

	@Autowired
	private MongoUserRepository users;

	@Autowired
	private ImageInfoService imageInfoService;

	@Autowired
	private StorageService storageService;

	@GetMapping("/api/post/{id}")
	@CrossOrigin
	@ResponseBody
	public ResponseEntity<Post> getPost(@PathVariable String id) {
		// TODO:
		var found = posts.findById(id);

		if (found.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Post>(found.get(), HttpStatus.OK);
	}

	@PostMapping("/api/post")
	public ResponseEntity<GenericResponse> makePost(@RequestBody PostInfo postInfo) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName().toLowerCase();
		User poster = users.findByEmail(email);

		// FIXME: Validate rating

		File imageTempFile = storageService.getFromTempStorage(postInfo.getFilename());

		String mimeType;
		BufferedImage image;

		try {
			mimeType = URLConnection.guessContentTypeFromStream((InputStream) new BufferedInputStream(new FileInputStream(imageTempFile)));
			image = ImageIO.read(imageTempFile);
		} catch (FileNotFoundException e1) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find image file. Make sure it was uploaded first.");
		} catch (IOException e1) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to open image file.");
		}

		Post post = new Post(postInfo);
		post.setPoster(poster);
		post.setMime(mimeType);
		post.setWidth(image.getWidth());
		post.setHeight(image.getHeight());
		post.setFilesize(imageTempFile.length());

		String newFilename;

		try {
			newFilename = imageInfoService.getUniqueImageHash(image) + imageInfoService.getMimeExtension(mimeType);
			storageService.storeFileToPermanentStorage(imageTempFile, newFilename);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file to permanent storage");
		}

		post.setFilename(newFilename);
		posts.insert(post);

		return new ResponseEntity<>(new GenericResponse(""), HttpStatus.CREATED);
	}

}