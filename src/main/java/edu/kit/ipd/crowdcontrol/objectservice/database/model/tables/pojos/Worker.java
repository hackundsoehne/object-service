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
public class Worker implements java.io.Serializable {

	private static final long serialVersionUID = 1365287272;

	private java.lang.Integer idWorker;
	private java.lang.String  identification;
	private java.lang.String  platform;
	private java.lang.String  email;
	private java.lang.Integer quality;

	public Worker() {}

	public Worker(
		java.lang.Integer idWorker,
		java.lang.String  identification,
		java.lang.String  platform,
		java.lang.String  email,
		java.lang.Integer quality
	) {
		this.idWorker = idWorker;
		this.identification = identification;
		this.platform = platform;
		this.email = email;
		this.quality = quality;
	}

	public java.lang.Integer getIdWorker() {
		return this.idWorker;
	}

	public void setIdWorker(java.lang.Integer idWorker) {
		this.idWorker = idWorker;
	}

	public java.lang.String getIdentification() {
		return this.identification;
	}

	public void setIdentification(java.lang.String identification) {
		this.identification = identification;
	}

	public java.lang.String getPlatform() {
		return this.platform;
	}

	public void setPlatform(java.lang.String platform) {
		this.platform = platform;
	}

	public java.lang.String getEmail() {
		return this.email;
	}

	public void setEmail(java.lang.String email) {
		this.email = email;
	}

	public java.lang.Integer getQuality() {
		return this.quality;
	}

	public void setQuality(java.lang.Integer quality) {
		this.quality = quality;
	}
}
