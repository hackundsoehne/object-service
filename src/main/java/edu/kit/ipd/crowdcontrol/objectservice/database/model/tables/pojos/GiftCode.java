/**
 * This class is generated by jOOQ
 */
package edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GiftCode implements java.io.Serializable {

	private static final long serialVersionUID = 1655184115;

	private java.lang.Integer idGiftCode;
	private java.lang.String  code;
	private java.lang.Integer amount;

	public GiftCode() {}

	public GiftCode(
		java.lang.Integer idGiftCode,
		java.lang.String  code,
		java.lang.Integer amount
	) {
		this.idGiftCode = idGiftCode;
		this.code = code;
		this.amount = amount;
	}

	public java.lang.Integer getIdGiftCode() {
		return this.idGiftCode;
	}

	public void setIdGiftCode(java.lang.Integer idGiftCode) {
		this.idGiftCode = idGiftCode;
	}

	public java.lang.String getCode() {
		return this.code;
	}

	public void setCode(java.lang.String code) {
		this.code = code;
	}

	public java.lang.Integer getAmount() {
		return this.amount;
	}

	public void setAmount(java.lang.Integer amount) {
		this.amount = amount;
	}
}