package pl.dogesoulseller.thegg.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import pl.dogesoulseller.thegg.api.model.Post;
import pl.dogesoulseller.thegg.repo.MongoPostRepository;

@Controller
public class ShowPageController {
	@Autowired
	private MongoPostRepository posts;

	@GetMapping("/show/{id}")
	public String showPost(Model model, @PathVariable String id) {
		System.out.println(id);
		Post post = posts.findById(id).get();
		model.addAttribute("post", post);
		return "/show";
	}

	@GetMapping("/show")
	public String showPostEmpty() {
		return "redirect:/search";
	}
}
