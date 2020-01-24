package ai.libs.jaicore.experiments.databasehandle;

import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.IDatabaseConfig;

public class ExperimenterMySQLHandle extends AExperimenterSQLHandle {

	public ExperimenterMySQLHandle(final IDatabaseAdapter adapter, final String tablename) {
		super(adapter, tablename);
	}

	public ExperimenterMySQLHandle(final IDatabaseConfig config) {
		super(config);
	}

}
