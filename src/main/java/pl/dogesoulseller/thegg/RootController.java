package pl.dogesoulseller.thegg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RootController {
	@Autowired
    private SessionRegistry sessionRegistry;

	public List<UserDetails> listLoggedInUsers() {
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
		List<UserDetails> allDetails = new ArrayList<UserDetails>();

        for (Object principal : allPrincipals) {
			if (principal instanceof UserDetails) {
				System.err.println(principal.toString());
				allDetails.add((UserDetails)principal);
			}
        }

		return allDetails;
    }

	@GetMapping("/root")
	public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
		model.addAttribute("name", name);
		var users = listLoggedInUsers();
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		var princ = (UserDetails) principal;

		if (princ.getUsername() == "admin") {
			System.err.println("logged in as admin");
		} else {
			System.err.println("not admin");
		}

		return "/root";
	}
}
