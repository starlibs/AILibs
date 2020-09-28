package ai.libs.jaicore.db.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.api4.java.datastructure.kvstore.IKVStore;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import ai.libs.jaicore.db.DBTester;
import ai.libs.jaicore.db.IDatabaseAdapter;

@TestMethodOrder(OrderAnnotation.class)
public class SqlAdapterTest extends DBTester {

	private static final String TABLE = "testtable";

	@ParameterizedTest
	@Order(1)
	@MethodSource("getDatabaseAdapters")
	public void testCreateTable(final IDatabaseAdapter adapter) throws SQLException, IOException {
		assertFalse(adapter.doesTableExist(TABLE), "The test table " + TABLE + " already exists. It should not exist. Please remove it mnaually and run the test again.");
		this.logger.info("Create table...");
		Map<String, String> types = new HashMap<>();
		types.put("id", "INT(10)");
		types.put("a", "VARCHAR(10)");
		adapter.createTable(TABLE, "id", Arrays.asList("a"), types, Arrays.asList());
		assertTrue(adapter.doesTableExist(TABLE), "The test table " + TABLE + " could not be created!");
	}

	@ParameterizedTest
	@Order(2)
	@MethodSource("getDatabaseAdapters")
	public void testInsertQuery(final IDatabaseAdapter adapter) throws SQLException {
		int numEntriesBefore = this.numEntries(adapter, TABLE);
		assertEquals(0, numEntriesBefore);
		this.logger.info("Insert into table...");
		adapter.insert("INSERT INTO " + TABLE + " (a) VALUES ('x')");
		assertEquals(1, this.numEntries(adapter, TABLE));
		Map<String, Object> vals = new HashMap<>();
		vals.put("a", "y");
		adapter.insert(TABLE, vals);
		int numEntriesAfter = this.numEntries(adapter, TABLE);
		assertEquals(2, numEntriesAfter);
	}

	@ParameterizedTest
	@Order(3)
	@MethodSource("getDatabaseAdapters")
	public void testSelectQuery(final IDatabaseAdapter adapter) throws SQLException {
		List<IKVStore> res = adapter.getResultsOfQuery("SELECT * FROM " + TABLE);
		if (res.isEmpty() || res.size() > 2) {
			fail("No result or too many results returned for select query.");
		}
		IKVStore store = res.get(0);
		assertEquals("ID not as expected.", "1", store.getAsString("id"));
		assertEquals("Column 'a' not as expected.", "x", store.getAsString("a"));
		store = res.get(1);
		assertEquals("ID not as expected.", "2", store.getAsString("id"));
		assertEquals("Column 'a' not as expected.", "y", store.getAsString("a"));
	}

	@ParameterizedTest
	@Order(4)
	@MethodSource("getDatabaseAdapters")
	public void testRemoveEntryQuery(final IDatabaseAdapter adapter) throws SQLException {
		int numEntriesBefore = this.numEntries(adapter, TABLE);
		assertEquals(2, numEntriesBefore);
		adapter.update("DELETE FROM " + TABLE + "");
		int numEntriesAfter = this.numEntries(adapter, TABLE);
		assertEquals("No entry added!", 0, numEntriesAfter);
	}

	@ParameterizedTest
	@Order(5)
	@MethodSource("getDatabaseAdapters")
	public void testDropTable(final IDatabaseAdapter adapter) throws SQLException {
		adapter.update("DROP TABLE " + TABLE);
	}

	public int numEntries(final IDatabaseAdapter adapter, final String table) throws SQLException {
		return adapter.getResultsOfQuery("SELECT COUNT(*) as num FROM " + table).get(0).getAsInt("num");
	}

}
