package pl.dogesoulseller.thegg.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

/**
 * User role
 */
@Document(collection = "user_roles")
public class Role implements GrantedAuthority {
	private static final long serialVersionUID = -590309416029050628L;

	@Id
	private String id;

	@Indexed(unique = true, sparse = true)
	private String name;

	private List<String> privileges;

	public Role(String name, Collection<String> privileges) {
		this.name = name;
		this.privileges = new ArrayList<>(privileges);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<String> getPrivileges() {
		return privileges;
	}

	public List<Privilege> getSecurityPrivileges() {
		ArrayList<Privilege> output = new ArrayList<>(privileges.size() + 1);

		// Role authority
		privileges.add(this.name);

		// Privilege authorities
		for (String privilege : privileges) {
			output.add(new Privilege(privilege));
		}

		return output;
	}

	@Override
	public String getAuthority() {
		return name;
	}
}
