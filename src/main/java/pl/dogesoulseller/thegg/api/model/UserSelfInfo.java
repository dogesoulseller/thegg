package pl.dogesoulseller.thegg.api.model;

import pl.dogesoulseller.thegg.user.User;
import pl.dogesoulseller.thegg.user.User.Pronouns;

/**
 * Represents user info that is safe to send to clients.
 * Emails shouldn't be set unless the user is getting info about their own account
 */
public class UserSelfInfo {
	private String email;
	private String username;
	private String bio;
	private Pronouns pronouns;

	public UserSelfInfo(String email, String username, String bio, Pronouns pronouns) {
		this.email = email;
		this.username = username;
		this.bio = bio;
		this.pronouns = pronouns;
	}

	/**
	 * Constructs using full user info, effectively stripping backend data
	 * @param user 
	 */
	public UserSelfInfo(User user) {
		// this.email = user.getUsername();
		this.username = user.getNonUniqueUsername();
		this.bio = user.getBio();
		this.pronouns = user.getPronouns();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBio() {
		return bio;
	}

	public Pronouns getPronouns() {
		return pronouns;
	}

	public String getUsername() {
		return username;
	}
}
