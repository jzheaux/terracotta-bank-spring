package com.joshcummings.codeplay.terracotta.config;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.authorizeRequests()
				.antMatchers("/passwordUpgradeDemo").permitAll()
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.and()
			.csrf().csrfTokenRepository(csrfTokenRepository())
				.and()
			.sessionManagement()
				.sessionAuthenticationStrategy(sessionAuthenticationStrategy());
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new OldPasswordEncoder();
	}

	@Bean
	CsrfTokenRepository csrfTokenRepository() {
		return new HttpSessionCsrfTokenRepository();
	}

	SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		SessionAuthenticationStrategy addToLegacySession =
				(authentication, request, response) -> {
					if ( authentication.getPrincipal() instanceof TerracottaUser ) {
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

	@Bean
	public UserDetailsService userDetailsService() {
		UserService userService = new UserService();
		return (username) ->
				new TerracottaUser(userService.findByUsername(username));
	}

	public static class TerracottaUser implements UserDetails {
		private final User user;

		public TerracottaUser(User user) {
			this.user = user;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return Collections.emptyList();
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

	private static class OldPasswordEncoder implements PasswordEncoder {
		QuietDigest digest = QuietDigest.getInstance("SHA-1");

		@Override
		public String encode(CharSequence rawPassword) {
			return this.digest.digest(rawPassword);
		}

		@Override
		public boolean matches(CharSequence rawPassword, String encodedPassword) {
			return rawPassword != null && this.encode(rawPassword).equals(encodedPassword);
		}
	}

	static class QuietDigest {
		MessageDigest digest;

		QuietDigest(MessageDigest digest) {
			this.digest = digest;
		}

		static QuietDigest getInstance(String algorithm) {
			try {
				MessageDigest d = MessageDigest.getInstance(algorithm);
				return new QuietDigest(d);
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
		}

		String digest(CharSequence value) {
			try {
				return new String(Hex.encode(this.digest.digest(Utf8.encode(value))));
			} catch ( Exception e ) {
				throw new RuntimeException(e);
			}
		}
	}
}
