package pl.dogesoulseller.thegg.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import pl.dogesoulseller.thegg.api.model.Post;

@RestController
public class SearchController {
	@GetMapping("/api/search")
	public ResponseEntity<Post[]> performSearch(@RequestParam(required = false) String query) {
		throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
	}
}
