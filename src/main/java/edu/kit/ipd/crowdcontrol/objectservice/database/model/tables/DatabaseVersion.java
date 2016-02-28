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
public class DatabaseVersion extends org.jooq.impl.TableImpl<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord> {

	private static final long serialVersionUID = -1746649120;

	/**
	 * The singleton instance of <code>crowdcontrol.Database_Version</code>
	 */
	public static final edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.DatabaseVersion DATABASE_VERSION = new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.DatabaseVersion();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord> getRecordType() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord.class;
	}

	/**
	 * The column <code>crowdcontrol.Database_Version.idDatabase_Version</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord, java.lang.Integer> IDDATABASE_VERSION = createField("idDatabase_Version", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Database_Version.version</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord, java.lang.Integer> VERSION = createField("version", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>crowdcontrol.Database_Version.timestamp</code>.
	 */
	public final org.jooq.TableField<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord, java.sql.Timestamp> TIMESTAMP = createField("timestamp", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaulted(true), this, "");

	/**
	 * Create a <code>crowdcontrol.Database_Version</code> table reference
	 */
	public DatabaseVersion() {
		this("Database_Version", null);
	}

	/**
	 * Create an aliased <code>crowdcontrol.Database_Version</code> table reference
	 */
	public DatabaseVersion(java.lang.String alias) {
		this(alias, edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.DatabaseVersion.DATABASE_VERSION);
	}

	private DatabaseVersion(java.lang.String alias, org.jooq.Table<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord> aliased) {
		this(alias, aliased, null);
	}

	private DatabaseVersion(java.lang.String alias, org.jooq.Table<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, edu.kit.ipd.crowdcontrol.objectservice.database.model.Crowdcontrol.CROWDCONTROL, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord, java.lang.Integer> getIdentity() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.IDENTITY_DATABASE_VERSION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord> getPrimaryKey() {
		return edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.KEY_DATABASE_VERSION_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.records.DatabaseVersionRecord>>asList(edu.kit.ipd.crowdcontrol.objectservice.database.model.Keys.KEY_DATABASE_VERSION_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.DatabaseVersion as(java.lang.String alias) {
		return new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.DatabaseVersion(alias, this);
	}

	/**
	 * Rename this table
	 */
	public edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.DatabaseVersion rename(java.lang.String name) {
		return new edu.kit.ipd.crowdcontrol.objectservice.database.model.tables.DatabaseVersion(name, null);
	}
}
