package ai.libs.jaicore.experiments.databasehandle;

import ai.libs.jaicore.db.sql.rest.IRestDatabaseConfig;
import ai.libs.jaicore.db.sql.rest.RestSqlAdapter;
import ai.libs.jaicore.experiments.IExperimentDatabaseHandle;

public class ExperimenterRestSQLHandle extends AExperimenterSQLHandle implements IExperimentDatabaseHandle {

	public ExperimenterRestSQLHandle(final IRestDatabaseConfig config) {
		this (new RestSqlAdapter(config), config.getTable());
	}

	public ExperimenterRestSQLHandle(final RestSqlAdapter adapter, final String tablename) {
		super (adapter, tablename);
	}
}
