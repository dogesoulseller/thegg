package pl.dogesoulseller.thegg.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.NewTagInfo;
import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.api.response.GenericResponse;
import pl.dogesoulseller.thegg.repo.MongoTagRepository;
import pl.dogesoulseller.thegg.service.ApiKeyVerificationService;
import pl.dogesoulseller.thegg.user.User;

import static pl.dogesoulseller.thegg.Utility.getServerBaseURL;

@RestController
public class TagController {
	private final MongoTagRepository tagRepository;
	private final ApiKeyVerificationService keyVerifier;

	public TagController(MongoTagRepository tagRepository, ApiKeyVerificationService keyVerifier) {
		this.tagRepository = tagRepository;
		this.keyVerifier = keyVerifier;
	}

	@GetMapping(value="/api/tag", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Tag> getTag(@RequestParam(required = false) String id, @RequestParam(required = false) String tag) {
		if ((id == null || id.isBlank()) && (tag == null || tag.isBlank())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id or tag must not be empty");
		}

		Tag result;

		// Id takes precendence
		if (id == null || id.isBlank()) {
			result = tagRepository.findByTag(tag);
			if (result == null) {
				throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find tag with tag " + tag);
			}
		} else {
			result = tagRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find tag with id " + id));
		}

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@PostMapping(value="/api/tag", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> createTag(@RequestParam String apikey, @RequestBody NewTagInfo info) {
		if (!keyVerifier.isValid(apikey)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}

		if (info.getTag().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag name must not be empty");
		}

		if (tagRepository.existsByTag(info.getTag())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "A tag with this name already exists");
		}

		Tag newTag = new Tag(info);

		newTag = tagRepository.insert(newTag);

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add("Location", getServerBaseURL() + "/api/tag?id=" + newTag.getId());

		return new ResponseEntity<>(new GenericResponse("Success"), headers, HttpStatus.CREATED);
	}
}
