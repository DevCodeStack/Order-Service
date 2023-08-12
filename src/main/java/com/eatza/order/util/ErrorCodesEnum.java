package com.eatza.order.util;

public enum ErrorCodesEnum {
	
	INTERNAL_SERVER_ERROR("EX500", "Internal service error occured. Please contact support for help"),
	AUTHENTICATION_ERROR("EX401", "Authentication failed for the user"),
	TOKEN_ERROR("EX900", "Invalid token"),
	SQL_UPDATE_FAILED("EX901", "Failed to update field");
	
	private String code;
	private String msg;
	
	ErrorCodesEnum(String code, String msg){
		this.setCode(code);
		this.setMsg(msg);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
