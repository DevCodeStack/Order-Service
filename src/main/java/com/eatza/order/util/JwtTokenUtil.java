package com.eatza.order.util;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.eatza.order.dto.ErrorResponseDto;
import com.eatza.order.exception.InvalidTokenException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;

@Component
@Setter
public class JwtTokenUtil implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1961936149219108137L;
	
	@Autowired
	RestTemplate restTemplate;
	
	@Value("${jwt.token-url.validate}")
	private String customerServiceTokenUrl;
	
	@Value("${jwt.secret}")
	private String secret;
	
	private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);
	
	public static String globalScopeToken;
	
	public Boolean validateToken(String token) throws InvalidTokenException {
		try {
			logger.debug("In validateToken method");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(customerServiceTokenUrl)
					.queryParam("token", token);
			HttpEntity<String> entity = new HttpEntity<>(headers);
			
			ResponseEntity<Boolean> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity, Boolean.class);
			globalScopeToken = token;
			return response.getBody();
			
		} catch(RestClientResponseException re) {
			if(re.getStatusCode() == HttpStatus.UNAUTHORIZED) {
				ObjectMapper mapper = new ObjectMapper();
				try {
					ErrorResponseDto errorResponseDto = 
							mapper.readValue(re.getResponseBodyAsString(), ErrorResponseDto.class);
					throw new InvalidTokenException(errorResponseDto.getDescription());
				} catch (JsonProcessingException e) {
					throw new InvalidTokenException(e.getMessage());
				}		
			}
			throw new InvalidTokenException(re.getMessage());
		} catch(Exception ex) {
			if(ex instanceof InvalidTokenException)
				throw (InvalidTokenException) ex;
			throw new InvalidTokenException(ex.getMessage());
		}
	}
	
}
