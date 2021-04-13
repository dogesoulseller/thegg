package pl.dogesoulseller.thegg.api.model.selfdata;

import org.springframework.data.annotation.PersistenceConstructor;
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

	/**
	 * Constructs using full user info, effectively stripping backend data
	 *
	 * @param user full user info
	 */
	public UserSelfInfo(User user) {
		this.username = user.getNonUniqueUsername();
		this.bio = user.getBio();
		this.pronouns = user.getPronouns();
	}

	@PersistenceConstructor
	public UserSelfInfo(String email, String username, String bio, Pronouns pronouns) {
		this.email = email;
		this.username = username;
		this.bio = bio;
		this.pronouns = pronouns;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getBio() {
		return this.bio;
	}

	public void setBio(String bio) {
		this.bio = bio;
	}

	public Pronouns getPronouns() {
		return this.pronouns;
	}

	public void setPronouns(Pronouns pronouns) {
		this.pronouns = pronouns;
	}
}
