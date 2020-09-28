package ai.libs.jaicore.experiments.databasehandle;

import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.sql.DatabaseAdapterFactory;
import ai.libs.jaicore.db.sql.IRestDatabaseConfig;

public class ExperimenterRestSQLHandle extends AExperimenterSQLHandle {

	public ExperimenterRestSQLHandle(final IRestDatabaseConfig config) {
		this (DatabaseAdapterFactory.get(config), config.getTable());
	}

	public ExperimenterRestSQLHandle(final IDatabaseAdapter adapter, final String tablename) {
		super (adapter, tablename);
	}
}
