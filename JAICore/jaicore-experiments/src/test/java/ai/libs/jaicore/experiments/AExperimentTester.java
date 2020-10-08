package ai.libs.jaicore.experiments;

import ai.libs.jaicore.db.DBTest;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.experiments.databasehandle.ExperimenterMySQLHandle;

public class AExperimentTester extends DBTest {

	public static final String TABLE = "exptable";

	protected String getTablename(final IDatabaseAdapter adapter) {
		String table = TABLE + "_" + adapter.getClass().getName().replace(".", "_");
		this.logger.info("Using table {}", table);
		return table;
	}

	public IExperimentDatabaseHandle getHandle(final IDatabaseAdapter adapter) {
		return new ExperimenterMySQLHandle(adapter, this.getTablename(adapter));
	}

	public IExperimentDatabaseHandle getHandle(final Object config) {
		return this.getHandle(this.reportConfigAndGetAdapter(config));
	}
}
