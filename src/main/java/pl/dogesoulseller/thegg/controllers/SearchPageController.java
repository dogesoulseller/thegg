package pl.dogesoulseller.thegg.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pl.dogesoulseller.thegg.QueryParser;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

@Controller
public class SearchPageController {
	@Autowired
	private MongoPostRepository posts;

	@GetMapping("/search")
	public String searchPosts(Model model, @RequestParam(required = false) String query, @RequestParam(required = false) Integer page) {
		// TODO: Get user preferences about page size
		Pageable pageN = PageRequest.of(page == null ? 0 : page, 30, Sort.by("creation_date").descending());
		List<Post> foundPosts;

		if (query == null || query.isBlank()) {
			foundPosts = posts.findAll(pageN).toList();
		} else {
			QueryParser parser = new QueryParser(query).parse();

			List<String> includedTags = parser.getIncludedTags();
			List<String> excludedTags = parser.getExcludedTags();

			if (excludedTags.isEmpty()) { // Filter by included
				foundPosts = posts.filterByTagQuery(includedTags, pageN);
			} else if (includedTags.isEmpty()) { // Filter by excluded
				foundPosts = posts.filterByExcludedTags(excludedTags, pageN);
			} else { // Filter by both
				foundPosts = posts.filterByTagQuery(includedTags, excludedTags, pageN);
			}
		}

		model.addAttribute("posts", foundPosts);

		return "/search";
	}
}
