package pl.dogesoulseller.thegg.api.model;

import lombok.*;
import pl.dogesoulseller.thegg.user.User;
import pl.dogesoulseller.thegg.user.User.Pronouns;

/**
 * Represents user info that is safe to send to clients.
 * Emails shouldn't be set unless the user is getting info about their own account
 */
@AllArgsConstructor
@Getter
@Setter
public class UserSelfInfo {
	private String email;
	private String username;
	private String bio;
	private Pronouns pronouns;

	/**
	 * Constructs using full user info, effectively stripping backend data
	 * @param user
	 */
	public UserSelfInfo(User user) {
		this.username = user.getNonUniqueUsername();
		this.bio = user.getBio();
		this.pronouns = user.getPronouns();
	}
}
