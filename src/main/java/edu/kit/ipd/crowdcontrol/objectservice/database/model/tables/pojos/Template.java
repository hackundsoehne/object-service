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
public class Template implements java.io.Serializable {

	private static final long serialVersionUID = -1986255840;

	private java.lang.Integer idTemplate;
	private java.lang.String  template;
	private java.lang.String  title;
	private java.lang.String  answerType;

	public Template() {}

	public Template(
		java.lang.Integer idTemplate,
		java.lang.String  template,
		java.lang.String  title,
		java.lang.String  answerType
	) {
		this.idTemplate = idTemplate;
		this.template = template;
		this.title = title;
		this.answerType = answerType;
	}

	public java.lang.Integer getIdTemplate() {
		return this.idTemplate;
	}

	public void setIdTemplate(java.lang.Integer idTemplate) {
		this.idTemplate = idTemplate;
	}

	public java.lang.String getTemplate() {
		return this.template;
	}

	public void setTemplate(java.lang.String template) {
		this.template = template;
	}

	public java.lang.String getTitle() {
		return this.title;
	}

	public void setTitle(java.lang.String title) {
		this.title = title;
	}

	public java.lang.String getAnswerType() {
		return this.answerType;
	}

	public void setAnswerType(java.lang.String answerType) {
		this.answerType = answerType;
	}
}
