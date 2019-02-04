package jaicore.experiments;

import java.io.IOException;
import java.sql.SQLException;

import jaicore.basic.SQLAdapter;

/**
 * Tests the class {@link ExperimentAdditionalTableLoader}. Integration tests
 * that need to be performed manually.
 * 
 * @author Helena Graf
 *
 */
public class ExperimentAdditionalTableLoaderTest {

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
	public static void testCreateTables(SQLAdapter adapter) throws IOException, SQLException {
		ExperimentAdditionalTableLoader.executeStatementsFromDirectory("test_resource", adapter);
		adapter.update("DROP TABLE IF EXISTS test");
		adapter.update("DROP TABLE IF EXISTS test2");
	}

	public static void main(String[] args) throws IOException, SQLException {
		SQLAdapter adapter = new SQLAdapter(args[0], args[1], args[2], args[3]);
		testCreateTables(adapter);
	}
}
