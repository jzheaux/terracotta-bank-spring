package com.joshcummings.codeplay.terracotta.config;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;

import java.util.Collection;
import java.util.Map;

/**
 * A simple usage of Spring Security OAuth 2 Login
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Autowired
	UserService userService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
					.anyRequest().authenticated().and()
				.oauth2Login()
					.loginPage("/oauth2/authorization/okta")
					.userInfoEndpoint().oidcUserService(oidcUserService()).and()
					.and()
				.sessionManagement()
					.sessionAuthenticationStrategy(sessionAuthenticationStrategy());
	}

	@Bean
	public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		SessionFixationProtectionStrategy strategy = new SessionFixationProtectionStrategy();
		return (authentication, request, response) -> {
			strategy.onAuthentication(authentication, request, response);

			if ( authentication.getPrincipal() instanceof TerracottaOidcUser ) {
				request.getSession().setAttribute("authenticatedUser",
						((TerracottaOidcUser) authentication.getPrincipal()).getUser());
			}
		};
	}

	@Bean
	public OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
		OidcUserService delegate = new OidcUserService();
		return request -> {
			OidcUser oidc = delegate.loadUser(request);

			// auto-provisioning is out of scope for this demo, assuming some additional
			// mechanism to persist federated users

			User user = this.userService.findByExternalUsername(oidc.getName());

			return new TerracottaOidcUser(oidc, user);
		};
	}

	public static class TerracottaOidcUser implements OidcUser {
		private final OidcUser oidc;
		private final User user;

		public TerracottaOidcUser(OidcUser oidc, User user) {
			this.oidc = oidc;
			this.user = user;
		}

		@Override
		public Map<String, Object> getClaims() {
			return this.oidc.getClaims();
		}

		@Override
		public OidcUserInfo getUserInfo() {
			return this.oidc.getUserInfo();
		}

		@Override
		public OidcIdToken getIdToken() {
			return this.oidc.getIdToken();
		}

		@Override
		public String getName() {
			return this.oidc.getName();
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			return this.oidc.getAuthorities();
		}

		@Override
		public Map<String, Object> getAttributes() {
			return this.oidc.getAttributes();
		}

		public User getUser() {
			return user;
		}
	}
}
