package pl.dogesoulseller.thegg.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.server.ResponseStatusException;
import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

@Controller
public class ShowPageController {
	private final MongoPostRepository posts;

	public ShowPageController(MongoPostRepository posts) {
		this.posts = posts;
	}

	@GetMapping("/show/{id}")
	public String showPost(Model model, @PathVariable String id) {
		Post post = posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("post", post);
		return "/show";
	}

	@GetMapping("/show")
	public String showPostEmpty() {
		return "redirect:/search";
	}
}
