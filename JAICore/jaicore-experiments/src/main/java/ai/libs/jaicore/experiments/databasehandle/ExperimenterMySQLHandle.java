package ai.libs.jaicore.experiments.databasehandle;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.db.sql.SQLAdapter;

public class ExperimenterMySQLHandle extends AExperimenterSQLHandle {

	public ExperimenterMySQLHandle(final SQLAdapter adapter, final String tablename) {
		super(adapter, tablename);
	}

	public ExperimenterMySQLHandle(final IDatabaseConfig config) {
		super(config);
	}

}
