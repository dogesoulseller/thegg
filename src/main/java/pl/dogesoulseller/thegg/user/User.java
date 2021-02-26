package pl.dogesoulseller.thegg.user;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import pl.dogesoulseller.thegg.api.model.UserSelfInfo;

@Document(collection = "users")
public class User implements UserDetails {
	private static final long serialVersionUID = 3692128324948914348L;

	@Document
	public class Pronouns {
		private String subjective;
		private String objective;
		private String possessive;

		public Pronouns(String subjective, String objective, String possessive) {
			this.subjective = subjective;
			this.objective = objective;
			this.possessive = possessive;
		}

		public String getObjective() {
			return objective;
		}

		public String getPossessive() {
			return possessive;
		}

		public String getSubjective() {
			return subjective;
		}
	}

	@Id
	private String id;

	@Indexed(unique = true, sparse = true)
	private String email;

	@Indexed(sparse = true)
	private String username;

	private String password;
	private String bio;

	private Instant creationTime;

	@Indexed
	private boolean accEnabled;

	@Indexed
	private boolean accLocked;

	// Only used in cases of breach or reset
	private boolean credExpired;

	private Pronouns pronouns;

	@DBRef
	private List<Role> roles;

	@PersistenceConstructor
	public User(String email, String username, String password, String bio, boolean accEnabled, boolean accLocked,
			boolean credExpired, Pronouns pronouns, Collection<Role> roles, Instant creationTime) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.bio = bio;
		this.accEnabled = accEnabled;
		this.accLocked = accLocked;
		this.credExpired = credExpired;
		this.pronouns = pronouns;
		this.roles = new ArrayList<>(roles);
		this.creationTime = creationTime;
	}

	public User(String email, String username, String password, Collection<Role> roles, Instant creationTime) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.roles = new ArrayList<>(roles);
		this.creationTime = creationTime;

		// TODO: Email verification
		this.accEnabled = true;
	}

	public User(String email, String username, String password, Role role, Instant creationTime) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.roles = new ArrayList<Role>();
		this.roles.add(role);
		this.creationTime = creationTime;

		// TODO: Email verification
		this.accEnabled = true;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();

		for (Role role : this.roles) {
			authorities.addAll(role.getSecurityPrivileges());
		}

		return authorities;
	}

	public String getId() {
		return id;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public String getUsername() {
		return this.email;
	}

	public String getNonUniqueUsername() {
		return this.username;
	}

	public Instant getCreationTime() {
		return creationTime;
	}

	@Override
	public boolean isAccountNonExpired() {
		// Accounts do not expire
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !this.accLocked;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return !this.credExpired;
	}

	@Override
	public boolean isEnabled() {
		return this.accEnabled;
	}

	public String getBio() {
		return bio;
	}

	public Pronouns getPronouns() {
		return pronouns;
	}

	public void update(UserSelfInfo updateInfo) {
		this.bio = updateInfo.getBio() == null ? this.bio : updateInfo.getBio();
		this.email = updateInfo.getEmail() == null ? this.email : updateInfo.getEmail();
		this.pronouns = updateInfo.getPronouns() == null ? this.pronouns : updateInfo.getPronouns();
		this.username = updateInfo.getUsername() == null ? this.username : updateInfo.getUsername();
	}
}
