/**
 * This class is generated by jOOQ
 */
package edu.kit.ipd.crowdcontrol.objectservice.database.model.tables;

import edu.kit.ipd.crowdcontrol.objectservice.database.model.Crowdcontrol;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys;
import edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.AnswerRecord;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Answer extends org.jooq.impl.TableImpl<AnswerRecord> {

	private static final long serialVersionUID = -1116266335;

	/**
	 * The singleton instance of <code>crowdcontrol.Answer</code>
	 */
	public static final Answer ANSWER = new Answer();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<AnswerRecord> getRecordType() {
		return AnswerRecord.class;
	}

	/**
	 * The column <code>crowdcontrol.Answer.idAnswer</code>.
	 */
	public final org.jooq.TableField<AnswerRecord, java.lang.Integer> IDANSWER = createField("idAnswer", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Answer.task</code>.
	 */
	public final org.jooq.TableField<AnswerRecord, java.lang.Integer> TASK = createField("task", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Answer.answer</code>.
	 */
	public final org.jooq.TableField<AnswerRecord, java.lang.String> ANSWER_ = createField("answer", org.jooq.impl.SQLDataType.CLOB.length(16777215).nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Answer.timestamp</code>.
	 */
	public final org.jooq.TableField<AnswerRecord, java.sql.Timestamp> TIMESTAMP = createField("timestamp", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>crowdcontrol.Answer.worker_id</code>.
	 */
	public final org.jooq.TableField<AnswerRecord, java.lang.Integer> WORKER_ID = createField("worker_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Answer.quality</code>.
	 */
	public final org.jooq.TableField<AnswerRecord, java.lang.Integer> QUALITY = createField("quality", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * Create a <code>crowdcontrol.Answer</code> table reference
	 */
	public Answer() {
		this("Answer", null);
	}

	/**
	 * Create an aliased <code>crowdcontrol.Answer</code> table reference
	 */
	public Answer(java.lang.String alias) {
		this(alias, Answer.ANSWER);
	}

	private Answer(java.lang.String alias, org.jooq.Table<AnswerRecord> aliased) {
		this(alias, aliased, null);
	}

	private Answer(java.lang.String alias, org.jooq.Table<AnswerRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, Crowdcontrol.CROWDCONTROL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<AnswerRecord> getPrimaryKey() {
		return Keys.KEY_ANSWER_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<AnswerRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<AnswerRecord>>asList(Keys.KEY_ANSWER_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<AnswerRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<AnswerRecord, ?>>asList(Keys.IDHITANSWERS, Keys.WORKERANSWERED);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Answer as(java.lang.String alias) {
		return new Answer(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Answer rename(java.lang.String name) {
		return new Answer(name, null);
	}
}