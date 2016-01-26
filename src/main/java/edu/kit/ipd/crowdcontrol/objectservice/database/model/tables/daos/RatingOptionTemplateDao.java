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
public class RatingOptionTemplateDao extends org.jooq.impl.DAOImpl<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.RatingOptionTemplateRecord, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate, java.lang.Integer> {

	/**
	 * Create a new RatingOptionTemplateDao without any configuration
	 */
	public RatingOptionTemplateDao() {
		super(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.RatingOptionTemplate.RATING_OPTION_TEMPLATE, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate.class);
	}

	/**
	 * Create a new RatingOptionTemplateDao with an attached configuration
	 */
	public RatingOptionTemplateDao(org.jooq.Configuration configuration) {
		super(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.RatingOptionTemplate.RATING_OPTION_TEMPLATE, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate.class, configuration);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected java.lang.Integer getId(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate object) {
		return object.getIdRatingOptionsTemplate();
	}

	/**
	 * Fetch records that have <code>id_rating_options_template IN (values)</code>
	 */
	public java.util.List<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate> fetchByIdRatingOptionsTemplate(java.lang.Integer... values) {
		return fetch(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.RatingOptionTemplate.RATING_OPTION_TEMPLATE.ID_RATING_OPTIONS_TEMPLATE, values);
	}

	/**
	 * Fetch a unique record that has <code>id_rating_options_template = value</code>
	 */
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate fetchOneByIdRatingOptionsTemplate(java.lang.Integer value) {
		return fetchOne(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.RatingOptionTemplate.RATING_OPTION_TEMPLATE.ID_RATING_OPTIONS_TEMPLATE, value);
	}

	/**
	 * Fetch records that have <code>name IN (values)</code>
	 */
	public java.util.List<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate> fetchByName(java.lang.String... values) {
		return fetch(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.RatingOptionTemplate.RATING_OPTION_TEMPLATE.NAME, values);
	}

	/**
	 * Fetch records that have <code>value IN (values)</code>
	 */
	public java.util.List<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate> fetchByValue(java.lang.Integer... values) {
		return fetch(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.RatingOptionTemplate.RATING_OPTION_TEMPLATE.VALUE, values);
	}

	/**
	 * Fetch records that have <code>template IN (values)</code>
	 */
	public java.util.List<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.pojos.RatingOptionTemplate> fetchByTemplate(java.lang.Integer... values) {
		return fetch(edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.RatingOptionTemplate.RATING_OPTION_TEMPLATE.TEMPLATE, values);
	}
}
