package pl.dogesoulseller.thegg;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // @Override
    // protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    // 	PasswordEncoder encoder =
	//     PasswordEncoderFactories.createDelegatingPasswordEncoder();
    // 	auth
	//     	.inMemoryAuthentication()
	//     	.withUser("user")
	//     	.password(encoder.encode("password"))
	//     	.roles("USER")
	//     	.and()
	//     	.withUser("admin")
	//     	.password(encoder.encode("admin"))
	//     	.roles("USER", "ADMIN");
    // }

	// TODO: Mongo auth
	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		List<UserDetails> users = new ArrayList<>();
		users.add(User.withDefaultPasswordEncoder()
			.username("user")
			.password("user")
			.roles("USER")
			.build());

		users.add(User.withDefaultPasswordEncoder()
			.username("admin")
			.password("admin")
			.roles("ADMIN")
			.build());

		return new InMemoryUserDetailsManager(users);
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
	http
		.sessionManagement().maximumSessions(1).sessionRegistry(sessionRegistry());
	http
	    .authorizeRequests()
			.antMatchers("/").permitAll()
			.anyRequest().authenticated()
			.and()
		.formLogin()
			.loginPage("/login")
			.permitAll()
			.and()
		.logout()
			.permitAll();
    }

	@Bean
    public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
        return new ServletListenerRegistrationBean<HttpSessionEventPublisher>(new HttpSessionEventPublisher());
    }
}
