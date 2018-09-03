package com.joshcummings.codeplay.terracotta.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.Filter;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.stereotype.Component;

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
	public DefaultAuthenticationEventPublisher authenticationEventPublisher() {
		return new DefaultAuthenticationEventPublisher();
	}

	@Component
	static class LoggerConfiguration {
		private final Logger logger = LoggerFactory.getLogger("authentication-event");

		@EventListener
		public void logAuthenticationFailure(AbstractAuthenticationFailureEvent failure) {
			Authentication attempt = failure.getAuthentication();
			logger.info("Authentication failed for " + attempt.getName() +
					" because " + failure.getException().getMessage());
		}

		@EventListener
		public void logAuthenticationSuccess(AuthenticationSuccessEvent success) {
			Authentication token = success.getAuthentication();
			logger.info("Authentication success for " + token.getName());
		}

		@EventListener
		public void logAuthorizationFailure(AuthorizationFailureEvent failure) {
			Authentication attempt = failure.getAuthentication();
			logger.info("Authorization failure for " + attempt.getName() +
					" having authorities " + attempt.getAuthorities() +
					" because " + failure.getAccessDeniedException().getMessage());
		}

		@EventListener
		public void logAuthorizationSuccess(AuthorizedEvent success) {
			Authentication token = success.getAuthentication();
			logger.info("Authorization success for " + token.getName() +
					" having authorities " + token.getAuthorities() +
					" to resource " + success.getSource());
		}

		@Autowired
		ApplicationContext context;

		private <T extends Filter> T getFilter(Class<T> filterClass) {
			FilterChainProxy filterChain = context.getBean(FilterChainProxy.class);
			return (T)filterChain.getFilters("/").stream()
					.filter(filterClass::isInstance)
					.findFirst().orElse(null);
		}

		@EventListener
		public void handle(ContextRefreshedEvent event) {
			FilterSecurityInterceptor fsi = getFilter(FilterSecurityInterceptor.class);
			fsi.setPublishAuthorizationSuccess(true);
			MethodSecurityInterceptor msi = context.getBean(MethodSecurityInterceptor.class);
			msi.setPublishAuthorizationSuccess(true);
		}
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
