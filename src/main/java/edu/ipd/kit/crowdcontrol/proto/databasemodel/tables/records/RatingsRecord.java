/**
 * This class is generated by jOOQ
 */
package edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RatingsRecord extends org.jooq.impl.UpdatableRecordImpl<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.RatingsRecord> implements org.jooq.Record6<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.sql.Timestamp, java.lang.Integer, java.lang.String> {

	private static final long serialVersionUID = 934219838;

	/**
	 * Setter for <code>crowdcontrolproto.Ratings.idRatings</code>.
	 */
	public void setIdratings(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>crowdcontrolproto.Ratings.idRatings</code>.
	 */
	public java.lang.Integer getIdratings() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>crowdcontrolproto.Ratings.hit_r</code>.
	 */
	public void setHitR(java.lang.Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>crowdcontrolproto.Ratings.hit_r</code>.
	 */
	public java.lang.Integer getHitR() {
		return (java.lang.Integer) getValue(1);
	}

	/**
	 * Setter for <code>crowdcontrolproto.Ratings.answer_r</code>.
	 */
	public void setAnswerR(java.lang.Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>crowdcontrolproto.Ratings.answer_r</code>.
	 */
	public java.lang.Integer getAnswerR() {
		return (java.lang.Integer) getValue(2);
	}

	/**
	 * Setter for <code>crowdcontrolproto.Ratings.timestamp</code>.
	 */
	public void setTimestamp(java.sql.Timestamp value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>crowdcontrolproto.Ratings.timestamp</code>.
	 */
	public java.sql.Timestamp getTimestamp() {
		return (java.sql.Timestamp) getValue(3);
	}

	/**
	 * Setter for <code>crowdcontrolproto.Ratings.rating</code>.
	 */
	public void setRating(java.lang.Integer value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>crowdcontrolproto.Ratings.rating</code>.
	 */
	public java.lang.Integer getRating() {
		return (java.lang.Integer) getValue(4);
	}

	/**
	 * Setter for <code>crowdcontrolproto.Ratings.workerID</code>.
	 */
	public void setWorkerid(java.lang.String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>crowdcontrolproto.Ratings.workerID</code>.
	 */
	public java.lang.String getWorkerid() {
		return (java.lang.String) getValue(5);
	}

	// -------------------------------------------------------------------------
	// Primary key information
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Record1<java.lang.Integer> key() {
		return (org.jooq.Record1) super.key();
	}

	// -------------------------------------------------------------------------
	// Record6 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.sql.Timestamp, java.lang.Integer, java.lang.String> fieldsRow() {
		return (org.jooq.Row6) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row6<java.lang.Integer, java.lang.Integer, java.lang.Integer, java.sql.Timestamp, java.lang.Integer, java.lang.String> valuesRow() {
		return (org.jooq.Row6) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS.IDRATINGS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field2() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS.HIT_R;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field3() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS.ANSWER_R;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.sql.Timestamp> field4() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS.TIMESTAMP;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field5() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS.RATING;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field6() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS.WORKERID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value1() {
		return getIdratings();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value2() {
		return getHitR();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value3() {
		return getAnswerR();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.sql.Timestamp value4() {
		return getTimestamp();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value5() {
		return getRating();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value6() {
		return getWorkerid();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RatingsRecord value1(java.lang.Integer value) {
		setIdratings(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RatingsRecord value2(java.lang.Integer value) {
		setHitR(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RatingsRecord value3(java.lang.Integer value) {
		setAnswerR(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RatingsRecord value4(java.sql.Timestamp value) {
		setTimestamp(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RatingsRecord value5(java.lang.Integer value) {
		setRating(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RatingsRecord value6(java.lang.String value) {
		setWorkerid(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RatingsRecord values(java.lang.Integer value1, java.lang.Integer value2, java.lang.Integer value3, java.sql.Timestamp value4, java.lang.Integer value5, java.lang.String value6) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached RatingsRecord
	 */
	public RatingsRecord() {
		super(edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS);
	}

	/**
	 * Create a detached, initialised RatingsRecord
	 */
	public RatingsRecord(java.lang.Integer idratings, java.lang.Integer hitR, java.lang.Integer answerR, java.sql.Timestamp timestamp, java.lang.Integer rating, java.lang.String workerid) {
		super(edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Ratings.RATINGS);

		setValue(0, idratings);
		setValue(1, hitR);
		setValue(2, answerR);
		setValue(3, timestamp);
		setValue(4, rating);
		setValue(5, workerid);
	}
}