package it.etoken.component.eosblock.exception;

import it.etoken.base.common.exception.MLExceptionEnum;

public enum MLEosBlockException implements MLExceptionEnum  {
	SUCCESS("0", "success"),
	PAY_FAILURE("101","支付失败"),;
	
	private MLEosBlockException(String code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	/**
	 * 错误码
	 */
	private String code;

	/**
	 * 错误信息
	 */
	private String msg;

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
