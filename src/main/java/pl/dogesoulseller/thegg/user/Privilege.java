package pl.dogesoulseller.thegg.user;

import org.springframework.security.core.GrantedAuthority;

/**
 * Single operation or set of operations permitted to a user
 */
public class Privilege implements GrantedAuthority {
	private static final long serialVersionUID = -2982345946461932778L;

	private String name;

	Privilege(String name) {
		this.name = name;
	}

	@Override
	public String getAuthority() {
		return "OP_" + name;
	}

}
