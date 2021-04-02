package pl.dogesoulseller.thegg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pl.dogesoulseller.thegg.service.SearchService;

@Controller
public class SearchPageController {
	private final SearchService searchService;

	public SearchPageController(SearchService searchService) {
		this.searchService = searchService;
	}

	@GetMapping("/search")
	public String searchPosts(Model model, @RequestParam(required = false) String query,
			@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer perPage) {

		var foundPosts = searchService.findPostsFromQuery(query, page, perPage);

		model.addAttribute("posts", foundPosts);
		model.addAttribute("pageCount", foundPosts.getTotalPages());
		model.addAttribute("pageNum", page == null ? 0 : page);
		model.addAttribute("perPage", perPage == null ? 30 : perPage);
		return "/search";
	}
}
