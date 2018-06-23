package com.example.demo.oauth.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class CustomTokenEnhancer implements TokenEnhancer {

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
		final Map<String, Object> additionalInfo = new HashMap<>();
		additionalInfo.put("id", user.getId());
		additionalInfo.put("username", user.getUsername());
		additionalInfo.put("roles", user.getEmail());
		
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
		return accessToken;
	}

	
}
