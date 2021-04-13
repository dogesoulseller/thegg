package pl.dogesoulseller.thegg.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.NewPostInfo;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;
import pl.dogesoulseller.thegg.service.ImageInfoService;
import pl.dogesoulseller.thegg.service.StorageService;
import pl.dogesoulseller.thegg.service.TagManagementService;
import pl.dogesoulseller.thegg.user.User;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;

import static pl.dogesoulseller.thegg.Utility.authenticateUserKey;
import static pl.dogesoulseller.thegg.Utility.getServerBaseURL;

@Api(tags = "Posts")
@RestController
public class PostController {
	private static final Logger log = org.slf4j.LoggerFactory.getLogger(PostController.class);
	private final MongoPostRepository posts;

	private final ImageInfoService imageInfoService;

	private final StorageService storageService;

	private final TagManagementService tagService;

	public PostController(MongoPostRepository posts, ImageInfoService imageInfoService, StorageService storageService, TagManagementService tagService) {
		this.posts = posts;
		this.imageInfoService = imageInfoService;
		this.storageService = storageService;
		this.tagService = tagService;
	}

	@ApiOperation(value = "Get post", notes = "Gets information about a post by its database ID.<br><br>This method requires no authentication.")
	@CrossOrigin
	@GetMapping(value = "/api/post", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Post> getPost(@RequestParam String id) {
		var strippedId = id.strip();
		var found = posts.findById(strippedId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find post with id " + strippedId));

		return new ResponseEntity<>(found, HttpStatus.OK);
	}

	@ApiOperation(value = "Delete post", notes = "Deletes a post. This method is meant for usage by individual clients.<br><br>Requires the supplied apikey to belong to the same user as the one making the post.")
	@DeleteMapping(value = "/api/post", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> deletePost(@RequestParam String apikey, @RequestParam String id) {
		User user = authenticateUserKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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

	@ApiOperation(value = "Create new post", notes = "Creates a new post using the supplied post info.<br><br>Creating a new post requires a file to be first sent to the server via the /api/send-file endpoint")
	@PostMapping(value = "/api/post", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> newPost(@RequestParam String apikey, @RequestBody NewPostInfo postInfo) {
		User user = authenticateUserKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
		post = posts.insert(post);

		log.info("Post inserted with filename {}", newFilename);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Location", getServerBaseURL() + "/api/post?id=" + post.getId());

		return new ResponseEntity<>(new GenericResponse("Post created"), headers, HttpStatus.CREATED);
	}

	@ApiOperation(value = "Update post info", notes = "Updates post using the supplied post info.<br><br>postInfo fields that are null are not modified.<br>postInfo fields that have data replace the post's fields.<br>Filename is ignored")
	@PatchMapping(value = "/api/post", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> modifyPost(@RequestParam String apikey, @RequestParam String id, @RequestBody NewPostInfo postInfo) {
		User user = authenticateUserKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		var strippedId = id.strip();
		var post = posts.findById(strippedId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find post with id " + strippedId));

		if (!post.getPoster().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		try {
			post.update(postInfo);
		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data found in request");
		}

		tagService.insertTags(post.getTags());
		posts.save(post);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ApiOperation(value = "Update post info", notes = "Updates post using the supplied post info.<br><br>Existing data is replaced with given data.<br>Filename is ignored")
	@PutMapping(value = "/api/post", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> modifyPostFull(@RequestParam String apikey, @RequestParam String id, @RequestBody NewPostInfo postInfo) {
		User user = authenticateUserKey(apikey);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		var strippedId = id.strip();
		var post = posts.findById(strippedId)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find post with id " + strippedId));

		if (!post.getPoster().equals(user.getId())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		try {
			post.updateFull(postInfo);
		} catch (RuntimeException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid data found in request");
		}

		tagService.insertTags(post.getTags());
		posts.save(post);

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}