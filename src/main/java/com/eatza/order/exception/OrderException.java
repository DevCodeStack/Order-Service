package com.eatza.order.exception;

import com.eatza.order.util.ErrorCodesEnum;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OrderException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3647095788591452095L;
	
	private ErrorCodesEnum error;

	public OrderException() {
		super(ErrorCodesEnum.INTERNAL_SERVER_ERROR.getMsg());
		this.error = ErrorCodesEnum.INTERNAL_SERVER_ERROR;
	}
	
	public OrderException(String message) {
		super(message);
		this.error = ErrorCodesEnum.INTERNAL_SERVER_ERROR;
	}
	
	public OrderException(String message, ErrorCodesEnum error) {
		super(message);
		this.error = error;
	}

}
