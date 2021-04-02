package pl.dogesoulseller.thegg.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RootPageController {
	private final SessionRegistry sessionRegistry;

	public RootPageController(SessionRegistry sessionRegistry) {
		this.sessionRegistry = sessionRegistry;
	}

	public List<UserDetails> listLoggedInUsers() {
		List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
		List<UserDetails> allDetails = new ArrayList<>();

		for (Object principal : allPrincipals) {
			if (principal instanceof UserDetails) {
				allDetails.add((UserDetails) principal);
			}
		}

		return allDetails;
	}

	@GetMapping("/root")
	public String greeting(@RequestParam(name = "name", required = false, defaultValue = "World") String name, Model model) {
		model.addAttribute("name", name);
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		var princ = (UserDetails) principal;

		if (princ.getUsername().equals("admin")) {
			return "/root";
		} else {
			return "forward:/";
		}
	}
}
