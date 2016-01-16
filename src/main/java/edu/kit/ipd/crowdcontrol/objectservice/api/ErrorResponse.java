package edu.kit.ipd.crowdcontrol.objectservice.api;

public class ErrorResponse {
	private String code;
	private String detail;

	public ErrorResponse(String code, String detail) {
		this.code = code;
		this.detail = detail;
	}

	public String getCode() {
		return code;
	}

	public String getDetail() {
		return detail;
	}
}
