package pl.dogesoulseller.thegg.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.response.PagedResults;
import pl.dogesoulseller.thegg.service.SearchService;

@RestController
public class SearchController {
	// private static final Logger log =
	// LoggerFactory.getLogger(SearchController.class);

	@Autowired
	private SearchService searchService;

	@GetMapping("/api/search/post")
	public ResponseEntity<PagedResults<Post>> searchPosts(@RequestParam(required = false) String query,
			@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer perPage) {

		var foundPosts = searchService.findPostsFromQuery(query, page, perPage);

		if (foundPosts == null || foundPosts.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}

		var pagedPosts = new PagedResults<>(foundPosts, page);

		return new ResponseEntity<>(pagedPosts, HttpStatus.OK);
	}

	@GetMapping("/api/search/user")
	public ResponseEntity<PagedResults<Post>> searchUsers() {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}

	@GetMapping("/api/search/tag")
	public ResponseEntity<PagedResults<Post>> searchTags() {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}
}
