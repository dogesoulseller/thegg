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
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.PostInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.service.ImageInfoService;
import pl.dogesoulseller.thegg.service.StorageService;
import pl.dogesoulseller.thegg.service.TagManagementService;
import pl.dogesoulseller.thegg.user.User;

@Api(tags = { "Posts" })
@RestController
@Slf4j
public class PostController {
	@Autowired
	private MongoPostRepository posts;

	@Autowired
	private ImageInfoService imageInfoService;

	@Autowired
	private StorageService storageService;

	@Autowired
	private ApiKeyVerificationService keyVerifier;

	@Autowired
	private TagManagementService tagService;

	@GetMapping("/api/post")
	@ApiOperation(value = "Get post", notes = "Gets information about a post by its database ID.<br><br>This method requires no authentication.")
	@CrossOrigin
	public ResponseEntity<Post> getPost(@RequestParam String id) {
		id = id.strip();
		var found = posts.findById(id);

		if (found.isEmpty()) {
			log.warn("Sent 404 - Failed to find post by id {}", id);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<Post>(found.get(), HttpStatus.OK);
	}

	@ApiOperation(value = "Delete post", notes = "Deletes a post. This method is meant for usage by individual clients.<br><br>Requires the supplied apikey to belong to the same user as the one making the post.")
	@DeleteMapping("/api/post")
	public ResponseEntity<GenericResponse> deletePost(@RequestParam String apikey, @RequestParam String id) {
		var user = keyVerifier.getKeyUser(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		id = id.strip();
		var post = posts.findById(id).orElse(null);
		if (post == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		if (post.getPoster() != user.getId()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		posts.deleteById(id);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Create new post", notes = "Creates a new post using the supplied post info. Creating a new post requires a file to be first sent to the server via the /api/send-file endpoint")
	@PostMapping("/api/post")
	public ResponseEntity<GenericResponse> newPost(@RequestParam String apikey, @RequestBody PostInfo postInfo) {
		User user = keyVerifier.getKeyUser(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		File imageTempFile = storageService.getFromTempStorage(postInfo.getFilename());

		String mimeType;
		BufferedImage image;

		try {
			mimeType = URLConnection.guessContentTypeFromStream(
					(InputStream) new BufferedInputStream(new FileInputStream(imageTempFile)));
			image = ImageIO.read(imageTempFile);
		} catch (FileNotFoundException e1) {
			log.error("Could not find temp image file {}", imageTempFile.getName());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"Could not find image file. Make sure it was uploaded first");
		} catch (IOException e1) {
			log.error("Failed to open temp image file {}", imageTempFile.getName());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to open image file");
		}

		Post post;

		try {
			post = new Post(postInfo);
			post.setPoster(user.getId());
			post.setMime(mimeType);
			post.setWidth(image.getWidth());
			post.setHeight(image.getHeight());
			post.setFilesize(imageTempFile.length());
		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating invalid");
		}

		String newFilename;

		try {
			newFilename = imageInfoService.getUniqueImageHash(image) + imageInfoService.getMimeExtension(mimeType);
			storageService.storeFileToPermanentStorage(imageTempFile, newFilename);
		} catch (Exception e) {
			log.error("Failed to store file {} to permanent storage", imageTempFile.getName());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Failed to store file to permanent storage");
		}

		tagService.insertTags(post.getTags());

		post.setFilename(newFilename);
		posts.insert(post);

		log.info("Post inserted with filename {}", newFilename);

		return new ResponseEntity<>(new GenericResponse(""), HttpStatus.CREATED);
	}
}