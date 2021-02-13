package pl.dogesoulseller.thegg.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pl.dogesoulseller.thegg.api.model.UserRegister;

@Controller
public class RegisterController {
	@GetMapping("/register")
	public String register(Model model) {
		var regInfo = new UserRegister();
		model.addAttribute("userinfo", regInfo);
		return "/register";
	}
}
