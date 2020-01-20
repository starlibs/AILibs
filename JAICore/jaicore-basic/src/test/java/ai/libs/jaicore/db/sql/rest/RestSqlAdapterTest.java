package ai.libs.jaicore.db.sql.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.api4.java.datastructure.kvstore.IKVStore;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.Tester;

public class RestSqlAdapterTest extends Tester {

	public static final String VAR_DB_HOST = "AILIBS_JAICORE_DB_REST_DB_HOST";
	public static final String VAR_DB_TOKEN = "AILIBS_JAICORE_DB_REST_DB_TOKEN";

	private static RestSqlAdapter adapter;

	private static final String SELECT_TABLE = "test_select_table";
	private static final String DELETE_FROM_INSERT_TABLE = "test_insert_table";
	private static final String CREATE_DROP_TABLE = "test_createdrop_table";

	@BeforeClass
	public static void setup() throws IOException {
		IRestDatabaseConfig config = ConfigFactory.create(IRestDatabaseConfig.class, FileUtil.readPropertiesFile(new File("testrsc/test.restSqlAdapter.properties")));
		if (config.getHost() == null || config.getHost().trim().isEmpty()) {
			String val = System.getenv(VAR_DB_HOST);
			LOGGER.info("Reading host from environment variable {}. Value: {}", VAR_DB_HOST, val);
			config.setProperty(IRestDatabaseConfig.K_REST_DB_HOST, val);
		}
		if (config.getToken() == null || config.getToken().trim().isEmpty()) {
			String val = System.getenv(VAR_DB_TOKEN);
			LOGGER.info("Reading token from environment variable {}. Value: {}", VAR_DB_TOKEN, val);
			config.setProperty(IRestDatabaseConfig.K_REST_DB_TOKEN, val);
		}

		if (config.getHost() == null || config.getToken() == null) {
			LOGGER.error("The host and the token for the REST DB connection could not be loaded. Either add the proper values to the properties file or via environment variables '{}' and '{}'", VAR_DB_HOST, VAR_DB_TOKEN);
			throw new IllegalArgumentException("Could not load host and token information information");
		} else {
			LOGGER.info("Carry out tests with server backend at {} with token {}.", config.getHost(), config.getToken());
		}
		adapter = new RestSqlAdapter(config);
	}

	@Test
	public void testSelectQuery() throws SQLException {
		List<IKVStore> res = adapter.getResultsOfQuery("SELECT * FROM " + SELECT_TABLE);
		if (res.isEmpty() || res.size() > 1) {
			fail("No result or too many results returned for select query.");
		}
		IKVStore store = res.get(0);
		assertEquals("ID not as expected.", "1", store.getAsString("id"));
		assertEquals("Column 'a' not as expected.", "1", store.getAsString("a"));
		assertEquals("Column 'b' not as expected.", "y", store.getAsString("b"));
		assertEquals("Column 'c' not as expected.", "3", store.getAsString("c"));
	}

	@Test
	public void testInsertQuery() throws SQLException {
		int numEntriesBefore = this.numEntries(DELETE_FROM_INSERT_TABLE);
		adapter.insert("INSERT INTO " + DELETE_FROM_INSERT_TABLE + " (y) VALUES (2)");
		int numEntriesAfter = this.numEntries(DELETE_FROM_INSERT_TABLE);
		assertTrue("No entry added!", numEntriesAfter > numEntriesBefore);
	}

	@Test
	public void testRemoveEntryQuery() throws SQLException {
		int numEntriesBefore = this.numEntries(DELETE_FROM_INSERT_TABLE);
		adapter.insert("DELETE FROM " + DELETE_FROM_INSERT_TABLE + " LIMIT 1");
		int numEntriesAfter = this.numEntries(DELETE_FROM_INSERT_TABLE);
		assertTrue("No entry added!", numEntriesAfter < numEntriesBefore);
	}

	@Test
	public void testCreateAndDropTable() throws SQLException {
		this.logger.info("Create table...");
		adapter.query("CREATE TABLE " + CREATE_DROP_TABLE + " (a VARCHAR(1))");
		this.logger.info("Insert into table...");
		adapter.insert("INSERT INTO " + CREATE_DROP_TABLE + " (a) VALUES ('x')");
		assertTrue("Table could not be created correctly", this.numEntries(CREATE_DROP_TABLE) > 0);
		adapter.query("DROP TABLE " + CREATE_DROP_TABLE);
	}

	public int numEntries(final String table) throws SQLException {
		return adapter.select("SELECT * FROM " + table).size();
	}

}
