package pl.dogesoulseller.thegg.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

@RestController
public class SearchController {
	// private static final Logger log = LoggerFactory.getLogger(SearchController.class);

	@Autowired
	private MongoPostRepository posts;

	@GetMapping("/api/search")
	public ResponseEntity<List<Post>> performSearch(@RequestParam(required = false) String query, @RequestParam(required = false) Integer page) {
		if (query == null) {
			// TODO: Element count per user preference
			Pageable pageN = PageRequest.of(page == null ? 0 : page, 30, Sort.by("creation_date"));
			var result = posts.findAll(pageN).toList();

			return new ResponseEntity<List<Post>>(result, HttpStatus.OK);
		}

		// TODO: Query processing

		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Search queries not implemented");
	}
}
