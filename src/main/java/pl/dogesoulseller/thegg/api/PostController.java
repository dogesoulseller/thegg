package pl.dogesoulseller.thegg.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.PostInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.service.ImageInfoService;
import pl.dogesoulseller.thegg.service.StorageService;
import pl.dogesoulseller.thegg.service.TagManagementService;
import pl.dogesoulseller.thegg.user.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;

@Api(tags = {"Posts"})
@RestController
public class PostController {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(PostController.class);
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
		var strippedId = id.strip();
		var found = posts.findById(strippedId)
		                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find post with id " + strippedId));

		return new ResponseEntity<>(found, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete post", notes = "Deletes a post. This method is meant for usage by individual clients.<br><br>Requires the supplied apikey to belong to the same user as the one making the post.")
	@DeleteMapping("/api/post")
	public ResponseEntity<GenericResponse> deletePost(@RequestParam String apikey, @RequestParam String id) {
		var user = keyVerifier.getKeyUser(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}

		var strippedId = id.strip();
		var post = posts.findById(strippedId)
		                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find post with id " + strippedId));

		if (!post.getPoster().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		posts.deleteById(strippedId);

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
			mimeType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(new FileInputStream(imageTempFile)));
			image = ImageIO.read(imageTempFile);
		} catch (FileNotFoundException e) {
			log.error("Could not find temp image file {}", imageTempFile.getName());
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find image file. Make sure it was uploaded first", e);
		} catch (IOException e) {
			log.error("Failed to open temp image file {}", imageTempFile.getName());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to open image file", e);
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
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating invalid", e);
		}

		String newFilename;

		try {
			newFilename = imageInfoService.getUniqueImageHash(image) + imageInfoService.getMimeExtension(mimeType);
			storageService.storeFileToPermanentStorage(imageTempFile, newFilename);
		} catch (Exception e) {
			log.error("Failed to store file {} to permanent storage", imageTempFile.getName());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file to permanent storage", e);
		}

		tagService.insertTags(post.getTags());

		post.setFilename(newFilename);
		posts.insert(post);

		log.info("Post inserted with filename {}", newFilename);

		return new ResponseEntity<>(new GenericResponse(""), HttpStatus.CREATED);
	}
}