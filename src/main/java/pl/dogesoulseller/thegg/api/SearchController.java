package pl.dogesoulseller.thegg.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.api.model.Tag;
import pl.dogesoulseller.thegg.api.response.PagedResults;
import pl.dogesoulseller.thegg.service.SearchService;

@Api(tags = "Search")
@RestController
public class SearchController {
	private final SearchService searchService;

	public SearchController(SearchService searchService) {
		this.searchService = searchService;
	}

	@ApiOperation("Search posts")
	@GetMapping(value = "/api/search/post", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PagedResults<Post>> searchPosts(@RequestParam(required = false) String query,
														  @RequestParam(required = false) Integer page,
														  @RequestParam(required = false) Integer perPage) {

		var foundPosts = searchService.findPostsFromQuery(query, page, perPage);

		if (foundPosts == null || foundPosts.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find a matching post");
		}

		var pagedPosts = new PagedResults<>(foundPosts, page == null ? 0 : page);

		return new ResponseEntity<>(pagedPosts, HttpStatus.OK);
	}

	@ApiOperation("Search tags")
	@GetMapping(value = "/api/search/tag", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PagedResults<Tag>> searchTags(@RequestParam(required = false) String query,
														@RequestParam(required = false) Integer page,
														@RequestParam(required = false) Integer perPage) {

		var foundTags = searchService.findTagFromQuery(query, page, perPage);
		if (foundTags == null || foundTags.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to find a matching tag");
		}

		var pagedTags = new PagedResults<>(foundTags, page == null ? 0 : page);

		return new ResponseEntity<>(pagedTags, HttpStatus.OK);
	}
}
