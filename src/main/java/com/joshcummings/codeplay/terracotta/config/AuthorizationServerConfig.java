package com.joshcummings.codeplay.terracotta.config;

import com.joshcummings.codeplay.terracotta.model.User;
import com.joshcummings.codeplay.terracotta.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.endpoint.WhitelabelApprovalEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * A pretty basic application of Spring Security OAuth's Authorization Server support
 *
 * @author Josh Cummings
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
	@Autowired
	AuthenticationManager authenticationManager;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints.authenticationManager(this.authenticationManager);
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients
				.inMemory()
					.withClient("qin")
						.secret("{noop}qin")
						.authorizedGrantTypes("authorization_code")
						.authorities("ROLE_CLIENT")
						.scopes("rest")
						.resourceIds("terracotta")
						.redirectUris("http://qin:8081/login/oauth2/code/terracotta");
	}
}



/**
 * For simplicity in demoing, this is isn't configured to protect all of Terracotta,
 * just the urls that are part of the Authorization Server approval flow
 *
 * Normally, this configuration would be taken care of simply by introducing
 * Spring Security into the application.
 */
@Configuration
@EnableWebSecurity
class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	UserService userService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.requestMatchers()
				.antMatchers("/oauth/authorize", "/login").and()
				.authorizeRequests()
				.anyRequest().authenticated().and()
				.formLogin();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return
				(username) -> {
					User user = this.userService.findByUsername(username);
					return new org.springframework.security.core.userdetails.User(
							username,
							"{noop}" + user.getPassword(),
							Arrays.asList(new SimpleGrantedAuthority("USER")));
				};
	}

	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
}

/**
 * A very low-tech way to customize Spring's default approval page, but for demo purposes, it'll work.
 *
 * Better would obviously be to create your own page. :)
 */
@Controller
@SessionAttributes("authorizationRequest")
class StyledApprovalEndpoint {
	WhitelabelApprovalEndpoint endpoint = new WhitelabelApprovalEndpoint() {
		@Override
		protected String createTemplate(Map<String, Object> model, HttpServletRequest request) {
			String template = super.createTemplate(model, request);
			return template.replaceFirst("<html>",
					"<html><head>" +
							"<link rel='stylesheet' href='/css/bootstrap.min.css'>" +
							"<link rel='stylesheet' href='/css/font-awesome.min.css'>" +
							"<link rel='stylesheet' href='/css/nivo-lightbox.css'>" +
							"<link rel='stylesheet' href='/css/nivo_themes/default/default.css'>" +
							"<link rel='stylesheet' href='/css/style.css'>" +
							"<link rel='stylesheet' href='/css/approval.css'>" +
							"<link href='http://fonts.googleapis.com/css?family=Raleway:400,300,600,700' rel='stylesheet' type='text/css'>" +
							"</head>")
					.replaceFirst("scope.rest", "Access to Your Account Balance");
		}
	};

	@GetMapping("/oauth/confirm_access")
	public ModelAndView confirmAccess(Map<String, Object> model, HttpServletRequest request) throws Exception {
		return this.endpoint.getAccessConfirmation(model, request);
	}
}
