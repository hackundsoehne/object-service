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
public class Platform extends org.jooq.impl.TableImpl<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord> {

	private static final long serialVersionUID = 1953220545;

	/**
	 * The singleton instance of <code>crowdcontrol.Platform</code>
	 */
	public static final edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Platform PLATFORM = new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Platform();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord> getRecordType() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord.class;
	}

	/**
	 * The column <code>crowdcontrol.Platform.id_platform</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord, java.lang.String> ID_PLATFORM = createField("id_platform", org.jooq.impl.SQLDataType.VARCHAR.length(191).nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Platform.name</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(191).nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Platform.render_calibrations</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord, java.lang.Boolean> RENDER_CALIBRATIONS = createField("render_calibrations", org.jooq.impl.SQLDataType.BIT.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Platform.needs_email</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord, java.lang.Boolean> NEEDS_EMAIL = createField("needs_email", org.jooq.impl.SQLDataType.BIT.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Platform.inactive</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord, java.lang.Boolean> INACTIVE = createField("inactive", org.jooq.impl.SQLDataType.BIT.nullable(false).defaulted(true), this, "");

	/**
	 * Create a <code>crowdcontrol.Platform</code> table reference
	 */
	public Platform() {
		this("Platform", null);
	}

	/**
	 * Create an aliased <code>crowdcontrol.Platform</code> table reference
	 */
	public Platform(java.lang.String alias) {
		this(alias, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Platform.PLATFORM);
	}

	private Platform(java.lang.String alias, org.jooq.Table<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord> aliased) {
		this(alias, aliased, null);
	}

	private Platform(java.lang.String alias, org.jooq.Table<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, edu.kit.ipd.crowdcontrol.objectservice.database.model.Crowdcontrol.CROWDCONTROL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord> getPrimaryKey() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.KEY_PLATFORM_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.PlatformRecord>>asList(edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.KEY_PLATFORM_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Platform as(java.lang.String alias) {
		return new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Platform(alias, this);
	}

	/**
	 * Rename this table
	 */
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Platform rename(java.lang.String name) {
		return new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.Platform(name, null);
	}
}
