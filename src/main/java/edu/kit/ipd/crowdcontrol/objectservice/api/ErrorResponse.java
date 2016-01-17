package edu.kit.ipd.crowdcontrol.objectservice.api;

/**
 * Represents a error response and will be encoded into JSON.
 *
 * @author Niklas Keller
 */
public class ErrorResponse {
	private String code;
	private String detail;

	/**
	 * @param code
	 * 		Short error code to make errors machine readable.
	 * @param detail
	 * 		Detailed error message for humans.
	 * @param args
	 * 		Arguments for {@link String#format(String, Object...)}.
	 */
	public ErrorResponse(String code, String detail, Object... args) {
		this.code = code;
		this.detail = String.format(detail, args);
	}

	/**
	 * @return Short error code to make errors machine readable.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return Detailed error message for humans.
	 */
	public String getDetail() {
		return detail;
	}
}
