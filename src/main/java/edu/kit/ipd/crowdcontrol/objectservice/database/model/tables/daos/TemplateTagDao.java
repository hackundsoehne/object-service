/**
 * This class is generated by jOOQ
 */
package edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.daos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class TemplateTagDao extends org.jooq.impl.DAOImpl<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.TemplateTagRecord, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag, java.lang.Integer> {

	/**
	 * Create a new TemplateTagDao without any configuration
	 */
	public TemplateTagDao() {
		super(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.TemplateTag.TEMPLATE_TAG, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag.class);
	}

	/**
	 * Create a new TemplateTagDao with an attached configuration
	 */
	public TemplateTagDao(org.jooq.Configuration configuration) {
		super(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.TemplateTag.TEMPLATE_TAG, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag object) {
		return object.getIdTemplateTag();
	}

	/**
	 * Fetch records that have <code>id_template_Tag IN (values)</code>
	 */
	public java.util.List<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag> fetchByIdTemplateTag(java.lang.Integer... values) {
		return fetch(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.TemplateTag.TEMPLATE_TAG.ID_TEMPLATE_TAG, values);
	}

	/**
	 * Fetch a unique record that has <code>id_template_Tag = value</code>
	 */
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag fetchOneByIdTemplateTag(java.lang.Integer value) {
		return fetchOne(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.TemplateTag.TEMPLATE_TAG.ID_TEMPLATE_TAG, value);
	}

	/**
	 * Fetch records that have <code>template IN (values)</code>
	 */
	public java.util.List<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag> fetchByTemplate(java.lang.Integer... values) {
		return fetch(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.TemplateTag.TEMPLATE_TAG.TEMPLATE, values);
	}

	/**
	 * Fetch records that have <code>tag IN (values)</code>
	 */
	public java.util.List<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.TemplateTag> fetchByTag(java.lang.String... values) {
		return fetch(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.TemplateTag.TEMPLATE_TAG.TAG, values);
	}
}
