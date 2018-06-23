package com.example.demo.oauth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import com.example.demo.oauth.config.CustomUserDetailsService;

@EnableResourceServer
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter implements Ordered {

	@Override

	public int getOrder() {

		return 3;

	}

	private static final String CLIENT_SECRET = "1234";

	private static final String RESOURCE_ID = "myRestApp";

	/**
	 * 
	 * DefaultTokenService configured as bean in AuthorizationServerConfiguration
	 * class
	 * 
	 */

	@Autowired
	private DefaultTokenServices tokenServices;

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {

		resources.tokenServices(tokenServices).resourceId(RESOURCE_ID);

	}

	@Autowired
	private CustomUserDetailsService userDetailService;

	@Autowired
	public PasswordEncoder passwordEncoder;

	public DaoAuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

		provider.setPasswordEncoder(passwordEncoder);
		provider.setUserDetailsService(userDetailService);

		return provider;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {

		http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and().authorizeRequests()
				.antMatchers("/", "/favicon.ico", "/**/*.png", "/**/*.gif", "/**/*.svg", "/**/*.jpg", "/**/*.html",
						"/**/*.css", "/**/*.js")
				.permitAll()
				.antMatchers("/h2_console/**").permitAll()
				.antMatchers("/oauth/token", "/api/auth/**").permitAll()
				.and()
				.authorizeRequests().anyRequest().authenticated()

				.and()

				.exceptionHandling().accessDeniedHandler(new OAuth2AccessDeniedHandler());

		// http.authenticationProvider(authenticationProvider());

		http.headers().frameOptions().disable();

	}

}