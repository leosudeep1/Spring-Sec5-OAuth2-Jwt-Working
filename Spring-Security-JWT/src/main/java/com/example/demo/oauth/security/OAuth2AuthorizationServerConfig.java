package com.example.demo.oauth.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import com.example.demo.oauth.config.CustomTokenEnhancer;
import com.example.demo.oauth.config.CustomUserDetailsService;

/**
 * @author Sud
 *	
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
	
	/* Note: below variables should not be static and final, 
	otherwise var will be populate with values which are configured in properties file.
	we have to store client secret in encoded format. 
 	Otherwise we will face error like -  Encoded password does not look like BCrypt*/
	
	@Value("${spring.jwt.client-name}")
	private String CLIENT_NAME;
	
	@Value("${security.jwt.client-secret}")
	private String CLIENT_SECRET; // ~schoolApp00
	
	@Value("${spring.jwt.client-resourceid}")
	private String RESOURCE_ID;
	
	@Autowired
	@Qualifier(BeanIds.AUTHENTICATION_MANAGER)
	private AuthenticationManager authenticationManager;
	
	/*
	 * We have pass userDetailService in AuthorizationServer also, because when we trying to get token by using refresh_token.
	 * at that time this AuthorizationServer will trying to get user information by executing loadUserByUsername method of UserDetailsService.
	 * This AuthorizationServer will get username which is associated with that access token, and pass to loadUserByUsername.
	 */
	@Autowired
	private CustomUserDetailsService userDetailService;

	@Autowired
	public PasswordEncoder passwordEncoder;
	
	
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
    	
    	TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        tokenEnhancerChain.setTokenEnhancers(
          Arrays.asList(tokenEnhancer(), accessTokenConverter()));
        
        endpoints.tokenStore(tokenStore())
                 .accessTokenConverter(accessTokenConverter())
                 .authenticationManager(authenticationManager)
                 .userDetailsService(userDetailService)
                 .allowedTokenEndpointRequestMethods(HttpMethod.GET,HttpMethod.POST).tokenEnhancer(tokenEnhancerChain);
    }
 
    
    
    @Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		security
		.tokenKeyAccess("permitAll()")
			.checkTokenAccess("isAuthenticated()").passwordEncoder(passwordEncoder)
			.allowFormAuthenticationForClients();
	}



	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory().withClient(CLIENT_NAME)
			.authorizedGrantTypes("password","refresh_token")
			.scopes("read","write")
			.secret(CLIENT_SECRET)
			.resourceIds(RESOURCE_ID)
			.accessTokenValiditySeconds(20)
			.refreshTokenValiditySeconds(50000);
	}
 
    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("123");
        return converter;
    }
 
    @Bean
    public TokenStore tokenStore() {
        return new JwtTokenStore(accessTokenConverter());
    }
    
    @Bean
    @Primary
    public DefaultTokenServices tokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        //defaultTokenServices.setAccessTokenValiditySeconds(30);
        return defaultTokenServices;
    }
    
    @Bean
    public TokenEnhancer tokenEnhancer() {
        return new CustomTokenEnhancer();
    }
}