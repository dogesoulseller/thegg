package pl.dogesoulseller.thegg.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.QueryParser;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

@RestController
public class SearchController {
	// private static final Logger log = LoggerFactory.getLogger(SearchController.class);

	@Autowired
	private MongoPostRepository posts;

	@GetMapping("/api/search")
	public ResponseEntity<List<Post>> performSearch(@RequestParam(required = false) String query) {
		if (query == null) {
			// TODO: Element count in request params
			// TODO: Offset in request params

			throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Search API not implemented");
			// return new ResponseEntity<List<Post>>(result, HttpStatus.OK);
		}

		// TODO: Query processing
		QueryParser parser = new QueryParser(query);

		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Search queries not implemented");
	}
}
