package ai.libs.jaicore.experiments.databasehandle;

import ai.libs.jaicore.db.sql.IRestDatabaseConfig;
import ai.libs.jaicore.db.sql.RestSqlAdapter;

public class ExperimenterRestSQLHandle extends AExperimenterSQLHandle {

	public ExperimenterRestSQLHandle(final IRestDatabaseConfig config) {
		this (new RestSqlAdapter(config), config.getTable());
	}

	public ExperimenterRestSQLHandle(final RestSqlAdapter adapter, final String tablename) {
		super (adapter, tablename);
	}
}
