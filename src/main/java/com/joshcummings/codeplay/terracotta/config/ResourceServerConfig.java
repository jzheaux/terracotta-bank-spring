package com.joshcummings.codeplay.terracotta.config;

import com.joshcummings.codeplay.terracotta.model.Account;
import com.joshcummings.codeplay.terracotta.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * A pretty basic application of Spring Security OAuth's Resource Server support
 *
 * @author Josh Cummings
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
			.requestMatchers().antMatchers("/rest/*")
				.and()
			.authorizeRequests()
				.anyRequest().access("#oauth2.hasScope('rest')");
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId("terracotta");
	}

}


/**
 * For simplicity in demoing, I've added an extra endpoint here which would
 * normally sit alongside all the other endpoints in the application.
 */
@RestController
class AccountController {
	@Autowired
	AccountService accountService;

	@GetMapping("/rest/accounts/{accountId}")
	public @ResponseBody
	Double account(@PathVariable("accountId") String accountId) {
		Account account = this.accountService.findById(accountId);
		if ( account != null ) {
			return account.getAmount().doubleValue();
		}
		return 0d;
	}
}
