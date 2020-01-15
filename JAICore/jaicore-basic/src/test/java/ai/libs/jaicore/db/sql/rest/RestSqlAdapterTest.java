package ai.libs.jaicore.db.sql.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.http.client.ClientProtocolException;
import org.api4.java.datastructure.kvstore.IKVStore;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.FileUtil;

public class RestSqlAdapterTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(RestSqlAdapterTest.class);

	private static IRestDatabaseConfig config;
	private static RestSqlAdapter adapter;

	private static final String SELECT_TABLE = "test_select_table";
	private static final String DELETE_FROM_INSERT_TABLE = "test_insert_table";
	private static final String CREATE_DROP_TABLE = "test_createdrop_table";

	@BeforeClass
	public static void setup() throws IOException {
		config = ConfigFactory.create(IRestDatabaseConfig.class, FileUtil.readPropertiesFile(new File("testrsc/test.restSqlAdapter.properties")));
		if (config.getHost() == null || config.getHost().trim().isEmpty()) {
			config.setProperty(IRestDatabaseConfig.K_REST_DB_HOST, System.getenv("REST_DB_HOST"));
		}
		if (config.getToken() == null || config.getToken().trim().isEmpty()) {
			config.setProperty(IRestDatabaseConfig.K_REST_DB_TOKEN, System.getenv("REST_DB_TOKEN"));
		}

		if (config.getHost() == null || config.getToken() == null) {
			LOGGER.error("The host and the token for the REST DB connection could not be loaded. Either add the proper values to the properties file or via environment variables 'REST_DB_HOST' and 'REST_DB_TOKEN'");
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
	public void testInsertQuery() throws ClientProtocolException, IOException, SQLException {
		int numEntriesBefore = this.numEntries(DELETE_FROM_INSERT_TABLE);
		adapter.insert("INSERT INTO " + DELETE_FROM_INSERT_TABLE + " (y) VALUES (2)");
		int numEntriesAfter = this.numEntries(DELETE_FROM_INSERT_TABLE);
		assertTrue("No entry added!", numEntriesAfter > numEntriesBefore);
	}

	@Test
	public void testRemoveEntryQuery() throws ClientProtocolException, IOException, SQLException {
		int numEntriesBefore = this.numEntries(DELETE_FROM_INSERT_TABLE);
		adapter.insert("DELETE FROM " + DELETE_FROM_INSERT_TABLE + " LIMIT 1");
		int numEntriesAfter = this.numEntries(DELETE_FROM_INSERT_TABLE);
		assertTrue("No entry added!", numEntriesAfter < numEntriesBefore);
	}

	@Test
	public void testCreateAndDropTable() throws ClientProtocolException, IOException, SQLException {
		System.out.println("Create table...");
		adapter.query("CREATE TABLE " + CREATE_DROP_TABLE + " (a VARCHAR(1))");
		System.out.println("Insert into table...");
		adapter.insert("INSERT INTO " + CREATE_DROP_TABLE + " (a) VALUES ('x')");
		assertTrue("Table could not be created correctly", this.numEntries(CREATE_DROP_TABLE) > 0);
		adapter.query("DROP TABLE " + CREATE_DROP_TABLE);
	}

	public int numEntries(final String table) throws ClientProtocolException, IOException, SQLException {
		return adapter.select("SELECT * FROM " + table).size();
	}

}
