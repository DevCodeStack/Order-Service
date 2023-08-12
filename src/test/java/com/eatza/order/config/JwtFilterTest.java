package com.eatza.order.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.eatza.order.dto.ErrorResponseDto;
import com.eatza.order.exception.InvalidTokenException;
import com.eatza.order.exception.UnauthorizedException;
import com.eatza.order.util.ErrorCodesEnum;
import com.eatza.order.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

	@Mock
	JwtTokenUtil tokenUtil;
	
	@InjectMocks
	JwtFilter filter;
	
	@Mock
	ObjectMapper objectMapper;
	
	MockHttpServletRequest request;
	MockHttpServletResponse response;
	MockFilterChain filterChain;
	
	@BeforeEach
	void setUp() throws Exception {
		
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = new MockFilterChain();
		objectMapper = new ObjectMapper();
	}

	@Test
	void doFilter_UnauthorizedException() throws Exception {
		request.addHeader("authorization", "Bearer token");
		
		when(tokenUtil.validateToken(any())).thenReturn(false);
		assertDoesNotThrow(() -> {filter.doFilter(request, response, filterChain);});
		ErrorResponseDto  errorResponseDto = 
				objectMapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
		assertEquals("Invalid token" , errorResponseDto.getDescription());
		
	}
	
	@Test
	void doFilter_InvalidTokenException() throws Exception {
		request.addHeader("authorization", "Bearer token");
		InvalidTokenException exception = 
				new InvalidTokenException("Error occured", ErrorCodesEnum.INTERNAL_SERVER_ERROR);
		
		when(tokenUtil.validateToken(any())).thenThrow(exception);
		assertDoesNotThrow(() -> {filter.doFilter(request, response, filterChain);});
		ErrorResponseDto  errorResponseDto = 
				objectMapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
		assertEquals("Error occured" , errorResponseDto.getDescription());
		
	}
	
	@Test
	void doFilter_Exception() throws Exception {
		request.addHeader("authorization", "Bearer token");
		
		when(tokenUtil.validateToken(any())).thenThrow(new RuntimeException("Error occured"));
		assertDoesNotThrow(() -> {filter.doFilter(request, response, filterChain);});
		ErrorResponseDto  errorResponseDto = 
				objectMapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
		assertEquals("Error occured" , errorResponseDto.getDescription());
		
	}
	
	@Test
	void doFilter_InvalidTokenException_NullHeader() throws Exception {
		UnauthorizedException exception = 
				new UnauthorizedException("Missing or invalid Authorization header", ErrorCodesEnum.INTERNAL_SERVER_ERROR);
		
		assertDoesNotThrow(() -> {filter.doFilter(request, response, filterChain);});
		ErrorResponseDto  errorResponseDto = 
				objectMapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
		assertEquals(exception.getMessage() , errorResponseDto.getDescription());
		
	}
	
	@Test
	void doFilter_InvalidTokenException_MissingBearer() throws Exception {
		request.addHeader("authorization", "token");
		
		assertDoesNotThrow(() -> {filter.doFilter(request, response, filterChain);});
		ErrorResponseDto  errorResponseDto = 
				objectMapper.readValue(response.getContentAsString(), ErrorResponseDto.class);
		assertEquals("Missing or invalid Authorization header" , errorResponseDto.getDescription());
	}
	
	@Test
	void doFilter_Success() {
		request.addHeader("authorization", "Bearer token");
		MockFilterChain filterChain = new MockFilterChain();
		
		when(tokenUtil.validateToken(any())).thenReturn(true);
		assertDoesNotThrow(() -> {filter.doFilter(request, response, filterChain);});
		
	}
}
