package com.joshcummings.codeplay.terracotta.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/admin/**").access("hasRole('ADMIN')")
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.and()
			.httpBasic()
				.and()
			.csrf().csrfTokenRepository(csrfTokenRepository())
				.and()
			.sessionManagement()
				.sessionAuthenticationStrategy(sessionAuthenticationStrategy());
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		DelegatingPasswordEncoder encoder =
				(DelegatingPasswordEncoder)PasswordEncoderFactories.createDelegatingPasswordEncoder();
		encoder.setDefaultPasswordEncoderForMatches(NoOpPasswordEncoder.getInstance());
		return encoder;
	}

	@Bean
	CsrfTokenRepository csrfTokenRepository() {
		return new HttpSessionCsrfTokenRepository();
	}

	SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		SessionAuthenticationStrategy addToLegacySession =
				(authentication, request, response) -> {
					if (authentication.getPrincipal() instanceof TerracottaUser) {
						request.getSession().setAttribute("authenticatedUser",
								((TerracottaUser) authentication.getPrincipal()).getUser());
					}
				};
		return new CompositeSessionAuthenticationStrategy(
				Arrays.asList(
						new ChangeSessionIdAuthenticationStrategy(),
						new CsrfAuthenticationStrategy(csrfTokenRepository()),
						addToLegacySession
				)
		);
	}

	@Value("${system.username}") String systemUsername;
	@Value("${system.password}") String systemPassword;

	@Bean
	public UserDetailsService userDetailsService() {
		UserService userService = new UserService();
		return (username) -> {
			User user;
			if ( systemUsername.equals(username) ) {
				user = new User("-1", systemUsername, "{bcrypt}" + systemPassword, "System User", "system@terracottabank");
			} else {
				user = userService.findByUsername(username);
			}

			if ( user == null ) {
				throw new UsernameNotFoundException("Could not find this user");
			}
			return new TerracottaUser(user);
		};
	}

	public static class TerracottaUser implements UserDetails {
		private final User user;

		public TerracottaUser(User user) {
			this.user = user;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return "system".equals(this.user.getUsername()) ?
					Collections.singletonList(new SimpleGrantedAuthority("ROLE_SYSTEM")) :
					Collections.emptyList();
		}

		@Override
		public String getPassword() {
			return this.user.getPassword();
		}

		@Override
		public String getUsername() {
			return this.user.getUsername();
		}

		@Override
		public boolean isAccountNonExpired() {
			return true;
		}

		@Override
		public boolean isAccountNonLocked() {
			return true;
		}

		@Override
		public boolean isCredentialsNonExpired() {
			return true;
		}

		@Override
		public boolean isEnabled() {
			return true;
		}

		public User getUser() {
			return this.user;
		}
	}
}
