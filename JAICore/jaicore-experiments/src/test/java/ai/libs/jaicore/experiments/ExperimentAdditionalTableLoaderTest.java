package ai.libs.jaicore.experiments;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.Test;

import ai.libs.jaicore.db.IDatabaseConfig;
import ai.libs.jaicore.db.sql.SQLAdapter;

/**
 * Tests the class {@link ExperimentAdditionalTableLoader}. Integration tests
 * that need to be performed manually.
 *
 * @author Helena Graf
 *
 */
public class ExperimentAdditionalTableLoaderTest {

	private static final IDatabaseConfig DB_CONFIG = null;

	/**
	 * Tries to create two tables and immediately deletes them afterwards (if they
	 * have been created).
	 *
	 * @param adapter
	 *            the connection to use for the test (IMPORTANT: in the database the
	 *            connection is set to, there should not already exist tables "test"
	 *            and "test2" otherwise they will be deleted by the test)
	 * @throws IOException
	 *             connection error
	 * @throws SQLException
	 *             sql error
	 */
	@Test
	public void testCreateTables() throws IOException, SQLException {
		SQLAdapter adapter = new SQLAdapter(DB_CONFIG);
		ExperimentAdditionalTableLoader.executeStatementsFromDirectory("test_resource", adapter);
		adapter.update("DROP TABLE IF EXISTS test");
		adapter.update("DROP TABLE IF EXISTS test2");
	}
}
