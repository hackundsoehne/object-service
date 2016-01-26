/**
 * This class is generated by jOOQ
 */
package edu.kit.ipd.crowdcontrol.objectservice.database.model.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.1" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ChosenTaskChooserParam extends org.jooq.impl.TableImpl<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord> {

	private static final long serialVersionUID = 2061981381;

	/**
	 * The singleton instance of <code>crowdcontrol.Chosen_Task_Chooser_Param</code>
	 */
	public static final edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ChosenTaskChooserParam CHOSEN_TASK_CHOOSER_PARAM = new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ChosenTaskChooserParam();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord> getRecordType() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord.class;
	}

	/**
	 * The column <code>crowdcontrol.Chosen_Task_Chooser_Param.id_Choosen_Task_Chooser_Param</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord, java.lang.Integer> ID_CHOOSEN_TASK_CHOOSER_PARAM = createField("id_Choosen_Task_Chooser_Param", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Chosen_Task_Chooser_Param.value</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord, java.lang.String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR.length(191).nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Chosen_Task_Chooser_Param.experiment</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord, java.lang.Integer> EXPERIMENT = createField("experiment", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Chosen_Task_Chooser_Param.param</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord, java.lang.Integer> PARAM = createField("param", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>crowdcontrol.Chosen_Task_Chooser_Param</code> table reference
	 */
	public ChosenTaskChooserParam() {
		this("Chosen_Task_Chooser_Param", null);
	}

	/**
	 * Create an aliased <code>crowdcontrol.Chosen_Task_Chooser_Param</code> table reference
	 */
	public ChosenTaskChooserParam(java.lang.String alias) {
		this(alias, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ChosenTaskChooserParam.CHOSEN_TASK_CHOOSER_PARAM);
	}

	private ChosenTaskChooserParam(java.lang.String alias, org.jooq.Table<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord> aliased) {
		this(alias, aliased, null);
	}

	private ChosenTaskChooserParam(java.lang.String alias, org.jooq.Table<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, edu.kit.ipd.crowdcontrol.objectservice.database.model.Crowdcontrol.CROWDCONTROL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord, java.lang.Integer> getIdentity() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.IDENTITY_CHOSEN_TASK_CHOOSER_PARAM;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord> getPrimaryKey() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.KEY_CHOSEN_TASK_CHOOSER_PARAM_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord>>asList(edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.KEY_CHOSEN_TASK_CHOOSER_PARAM_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.ForeignKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.ChosenTaskChooserParamRecord, ?>>asList(edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.TASKCHOOSERPARAMREFEXPERIMENT, edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.CHOOSENTASKCHOOSERPARAM);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ChosenTaskChooserParam as(java.lang.String alias) {
		return new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ChosenTaskChooserParam(alias, this);
	}

	/**
	 * Rename this table
	 */
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ChosenTaskChooserParam rename(java.lang.String name) {
		return new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.ChosenTaskChooserParam(name, null);
	}
}
