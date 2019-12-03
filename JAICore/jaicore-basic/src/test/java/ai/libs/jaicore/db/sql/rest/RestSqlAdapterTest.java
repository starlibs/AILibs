package ai.libs.jaicore.db.sql.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.aeonbits.owner.ConfigFactory;
import org.apache.http.client.ClientProtocolException;
import org.junit.BeforeClass;
import org.junit.Test;

import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.kvstore.IKVStore;

public class RestSqlAdapterTest {

	private static IRestDatabaseConfig config;
	private static RestSqlAdapter adapter;

	private static final String SELECT_TABLE = "test_select_table";
	private static final String DELETE_FROM_INSERT_TABLE = "test_insert_table";
	private static final String CREATE_DROP_TABLE = "test_createdrop_table";

	@BeforeClass
	public static void setup() throws IOException {
		config = ConfigFactory.create(IRestDatabaseConfig.class, FileUtil.readPropertiesFile(new File("testrsc/test.restSqlAdapter.properties")));
		adapter = new RestSqlAdapter(config);
	}

	@Test
	public void testSelectQuery() throws ClientProtocolException, IOException {
		List<IKVStore> res = adapter.select("SELECT * FROM " + SELECT_TABLE);
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
	public void testInsertQuery() throws ClientProtocolException, IOException {
		int numEntriesBefore = this.numEntries(DELETE_FROM_INSERT_TABLE);
		adapter.insert("INSERT INTO " + DELETE_FROM_INSERT_TABLE + " (y) VALUES (2)");
		int numEntriesAfter = this.numEntries(DELETE_FROM_INSERT_TABLE);
		assertTrue("No entry added!", numEntriesAfter > numEntriesBefore);
	}

	@Test
	public void testRemoveEntryQuery() throws ClientProtocolException, IOException {
		int numEntriesBefore = this.numEntries(DELETE_FROM_INSERT_TABLE);
		adapter.insert("DELETE FROM " + DELETE_FROM_INSERT_TABLE + " LIMIT 1");
		int numEntriesAfter = this.numEntries(DELETE_FROM_INSERT_TABLE);
		assertTrue("No entry added!", numEntriesAfter < numEntriesBefore);
	}

	@Test
	public void testCreateAndDropTable() throws ClientProtocolException, IOException {
		System.out.println("Create table...");
		adapter.query("CREATE TABLE " + CREATE_DROP_TABLE + " (a VARCHAR(1))");
		System.out.println("Insert into table...");
		adapter.insert("INSERT INTO " + CREATE_DROP_TABLE + " (a) VALUES ('x')");
		assertTrue("Table could not be created correctly", this.numEntries(CREATE_DROP_TABLE) > 0);
		adapter.query("DROP TABLE " + CREATE_DROP_TABLE);
	}

	public int numEntries(final String table) throws ClientProtocolException, IOException {
		return adapter.select("SELECT * FROM " + table).size();
	}

}
