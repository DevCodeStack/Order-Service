package com.eatza.order.exception;

import com.eatza.order.util.ErrorCodesEnum;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UnauthorizedException extends RuntimeException {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6239448597666255913L;
	
	private ErrorCodesEnum error;

	public UnauthorizedException() {
		super(ErrorCodesEnum.INTERNAL_SERVER_ERROR.getMsg());
		this.error = ErrorCodesEnum.INTERNAL_SERVER_ERROR;
	}
	
	public UnauthorizedException(String message) {
		super(message);
		this.error = ErrorCodesEnum.INTERNAL_SERVER_ERROR;
	}
	
	public UnauthorizedException(String message, ErrorCodesEnum error) {
		super(message);
		this.error = error;
	}

}
