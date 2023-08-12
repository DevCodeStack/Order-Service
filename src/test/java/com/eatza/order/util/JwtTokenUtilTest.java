package com.eatza.order.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.eatza.order.dto.ErrorResponseDto;
import com.eatza.order.exception.InvalidTokenException;

@ExtendWith(MockitoExtension.class)
class JwtTokenUtilTest {

	@Mock
	RestTemplate restTemplate;
	
	@InjectMocks
	JwtTokenUtil tokenUtil;
	
	private String customerServiceTokenUrl = "http://tokenUrl";
	
	private String secret = "secretKey";
	
	@BeforeEach
	void setUp() throws Exception {
		tokenUtil.setCustomerServiceTokenUrl(customerServiceTokenUrl);
		tokenUtil.setSecret(secret);
	}

	//Positive test case : validateToken
	@Test
	void validateToken_Success() {
		String token = "token";
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<Boolean>>any()))
			.thenReturn(ResponseEntity.ok(true));
		
		Boolean status = tokenUtil.validateToken(token);
		assertThat(status);
		
	}
	
	//Negative test case : validateToken
	@Test
	void validateToken_CustomException() {
		String token = "token";
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<Boolean>>any()))
			.thenThrow(new InvalidTokenException("Customer not present or active"));
		
		assertThrows(InvalidTokenException.class, () -> {tokenUtil.validateToken(token);});
		
	}
	
	//Negative test case : validateToken
	@Test
	void validateToken_Exception() {
		String token = "token";
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<Boolean>>any()))
			.thenThrow(new RuntimeException());
		
		assertThrows(InvalidTokenException.class, () -> {tokenUtil.validateToken(token);});
		
	}
	
	//Negative test case : validateToken
	@Test
	void validateToken_RestClientResponseException() {
		String token = "token";
		ErrorResponseDto errorResponseDto = new ErrorResponseDto("EX900", "Invalid token", "JWT expired");
		
		RestClientResponseException clientResponseException = 
				new RestClientResponseException("Error occured", 401, "", null, errorResponseDto.toString().getBytes(), null);
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<Boolean>>any()))
			.thenThrow(clientResponseException);
		
		assertThrows(InvalidTokenException.class, () -> {tokenUtil.validateToken(token);});
		
		clientResponseException = new RestClientResponseException("Error occured", 400, "", null, errorResponseDto.toString().getBytes(), null);
		
		when(restTemplate.exchange(anyString(), 
				ArgumentMatchers.any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<?>>any(), 
				ArgumentMatchers.<Class<Boolean>>any()))
			.thenThrow(clientResponseException);
		
		assertThrows(InvalidTokenException.class, () -> {tokenUtil.validateToken(token);});
		
	}

}
