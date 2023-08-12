package com.eatza.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.eatza.order.dto.ErrorResponseDto;
import com.eatza.order.util.ErrorCodesEnum;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler{

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ErrorResponseDto> exception(UnauthorizedException exception) {
		log.debug("Handling UnauthorizedException");
		 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				 .body(new ErrorResponseDto(exception.getError().getCode(), 
						 exception.getError().getMsg(),
						 exception.getMessage()));
	}
	
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<ErrorResponseDto> exception(InvalidTokenException exception) {
		log.debug("Handling InvalidTokenException");
		 return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				 .body(new ErrorResponseDto(exception.getError().getCode(), 
						 exception.getError().getMsg(),
						 exception.getMessage()));
	}
	
	@ExceptionHandler(OrderException.class)
	public ResponseEntity<ErrorResponseDto> exception(OrderException exception) {
		log.debug("Handling CustomerException");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				 .body(new ErrorResponseDto(exception.getError().getCode(), 
						 exception.getError().getMsg(),
						 exception.getMessage()));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponseDto> exception(Exception exception) {
		log.debug("Handling Default Exception");
		 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				 .body(new ErrorResponseDto(ErrorCodesEnum.INTERNAL_SERVER_ERROR.getCode(), 
						 ErrorCodesEnum.INTERNAL_SERVER_ERROR.getMsg(), 
						 exception.getMessage()));
	}
	
}
