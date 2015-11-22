/**
 * This class is generated by jOOQ
 */
package edu.ipd.kit.crowdcontrol.proto.databasemodel.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Hit extends org.jooq.impl.TableImpl<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord> {

	private static final long serialVersionUID = 2089877641;

	/**
	 * The singleton instance of <code>crowdcontrolproto.HIT</code>
	 */
	public static final edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit HIT = new edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord> getRecordType() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord.class;
	}

	/**
	 * The column <code>crowdcontrolproto.HIT.idHIT</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.Integer> IDHIT = createField("idHIT", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.experiment_h</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.Integer> EXPERIMENT_H = createField("experiment_h", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.type</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.String> TYPE = createField("type", org.jooq.impl.SQLDataType.VARCHAR.length(45).nullable(false), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.running</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.Boolean> RUNNING = createField("running", org.jooq.impl.SQLDataType.BIT.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.current_amount</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.Integer> CURRENT_AMOUNT = createField("current_amount", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.max_amount</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.Integer> MAX_AMOUNT = createField("max_amount", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.payment</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.Integer> PAYMENT = createField("payment", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.bonus</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.Integer> BONUS = createField("bonus", org.jooq.impl.SQLDataType.INTEGER.nullable(false).defaulted(true), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.id_crowd_platform</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.String> ID_CROWD_PLATFORM = createField("id_crowd_platform", org.jooq.impl.SQLDataType.VARCHAR.length(45), this, "");

	/**
	 * The column <code>crowdcontrolproto.HIT.crowd_platform</code>.
	 */
	public final org.jooq.TableField<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, java.lang.String> CROWD_PLATFORM = createField("crowd_platform", org.jooq.impl.SQLDataType.VARCHAR.length(45).nullable(false), this, "");

	/**
	 * Create a <code>crowdcontrolproto.HIT</code> table reference
	 */
	public Hit() {
		this("HIT", null);
	}

	/**
	 * Create an aliased <code>crowdcontrolproto.HIT</code> table reference
	 */
	public Hit(java.lang.String alias) {
		this(alias, edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit.HIT);
	}

	private Hit(java.lang.String alias, org.jooq.Table<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord> aliased) {
		this(alias, aliased, null);
	}

	private Hit(java.lang.String alias, org.jooq.Table<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, edu.ipd.kit.crowdcontrol.proto.databasemodel.Crowdcontrolproto.CROWDCONTROLPROTO, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord> getPrimaryKey() {
		return edu.ipd.kit.crowdcontrol.proto.databasemodel.Keys.KEY_HIT_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord>>asList(edu.ipd.kit.crowdcontrol.proto.databasemodel.Keys.KEY_HIT_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.records.HitRecord, ?>>asList(edu.ipd.kit.crowdcontrol.proto.databasemodel.Keys.IDEXPERIMENTHIT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit as(java.lang.String alias) {
		return new edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit(alias, this);
	}

	/**
	 * Rename this table
	 */
	public edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit rename(java.lang.String name) {
		return new edu.ipd.kit.crowdcontrol.proto.databasemodel.tables.Hit(name, null);
	}
}