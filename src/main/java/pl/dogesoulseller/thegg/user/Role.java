package pl.dogesoulseller.thegg.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

	@PersistenceConstructor
	public Role(String id, String name, List<String> privileges) {
		this.id = id;
		this.name = name;
		this.privileges = privileges;
	}

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

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPrivileges(List<String> privileges) {
		this.privileges = privileges;
	}
}
