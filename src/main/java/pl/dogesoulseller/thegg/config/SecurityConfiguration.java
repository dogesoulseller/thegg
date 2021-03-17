package pl.dogesoulseller.thegg.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.session.HttpSessionEventPublisher;

import pl.dogesoulseller.thegg.service.SecureUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private SecureUserDetailsService userDetailsService;

	@Autowired
	private PasswordEncoder argonPasswordEncoder;

	@Bean
	public static SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Autowired
	public void configAuthBuilder(AuthenticationManagerBuilder builder) throws Exception {
		builder.userDetailsService(userDetailsService).passwordEncoder(argonPasswordEncoder);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// CSRF disabled on REST API and login page
		http.csrf().ignoringAntMatchers("/api/**", "/login");
		http.sessionManagement().maximumSessions(1).sessionRegistry(sessionRegistry());
		http
				.authorizeRequests()
				.antMatchers("/root").authenticated()
				.anyRequest().permitAll()
				.and()
				.formLogin()
				.loginPage("/login")
				.usernameParameter("email")
				.permitAll()
				.and()
				.logout()
				.permitAll();
	}

	@Bean
	public ServletListenerRegistrationBean<HttpSessionEventPublisher> httpSessionEventPublisher() {
		return new ServletListenerRegistrationBean<>(new HttpSessionEventPublisher());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new Argon2PasswordEncoder(16, 32, 1, 1 << 16, 4);
	}

	// API rqeuires use of HTTP basic authentication on key management
	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {
		protected void configure(HttpSecurity http) throws Exception {
			// CSRF disabled on REST API and login page
			http.csrf().ignoringAntMatchers("/api/**", "/login");
			http.sessionManagement().maximumSessions(1).sessionRegistry(sessionRegistry());

			// Require basic auth
			http.
					    requestMatchers().
							                     regexMatchers("/api/apikey").
									                                                 and()
			    .authorizeRequests(authorize -> authorize.anyRequest().authenticated()).httpBasic();
		}
	}
}
