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

import ai.libs.jaicore.db.DBTest;
import ai.libs.jaicore.db.IDatabaseAdapter;

@TestMethodOrder(OrderAnnotation.class)
public class SqlAdapterTest extends DBTest {

	private static final String TABLE = "testtable";

	private String getTablename(final IDatabaseAdapter adapter) {
		String table = TABLE + "_" + adapter.getClass().getName().replace(".", "_");
		this.logger.info("Using table {}" , table);
		return table;
	}

	@ParameterizedTest(name="create table")
	@Order(1)
	@MethodSource("getDatabaseConfigs")
	public void testCreateTable(final Object config) throws SQLException, IOException {
		IDatabaseAdapter adapter = this.reportConfigAndGetAdapter(config);
		String table = this.getTablename(adapter);
		if (adapter.doesTableExist(table)) {
			this.logger.warn("The test table " + table + " already exists. It should not exist and will be removed now prior to the test.");
			adapter.update("DROP TABLE `" + table + "`");
			this.logger.info("Table dropped, now continuing with the test.");
		}
		this.logger.info("Create table...");
		Map<String, String> types = new HashMap<>();
		types.put("id", "INT(10)");
		types.put("a", "VARCHAR(10)");
		adapter.createTable(table, "id", Arrays.asList("a"), types, Arrays.asList());
		assertTrue(adapter.doesTableExist(table), "The test table " + table + " could not be created!");
	}

	@ParameterizedTest(name="insert rows")
	@Order(2)
	@MethodSource("getDatabaseConfigs")
	public void testInsertQuery(final Object config) throws SQLException {
		IDatabaseAdapter adapter = this.reportConfigAndGetAdapter(config);
		String table = this.getTablename(adapter);
		int numEntriesBefore = this.numEntries(adapter, table);
		assertEquals(0, numEntriesBefore);
		this.logger.info("Insert into table...");
		adapter.insert("INSERT INTO " + table + " (a) VALUES ('x')");
		assertEquals(1, this.numEntries(adapter, table));
		Map<String, Object> vals = new HashMap<>();
		vals.put("a", "y");
		adapter.insert(table, vals);
		int numEntriesAfter = this.numEntries(adapter, table);
		assertEquals(2, numEntriesAfter);
	}

	@ParameterizedTest(name="select rows")
	@Order(3)
	@MethodSource("getDatabaseConfigs")
	public void testSelectQuery(final Object config) throws SQLException {
		IDatabaseAdapter adapter = this.reportConfigAndGetAdapter(config);
		String table = this.getTablename(adapter);
		List<IKVStore> res = adapter.getResultsOfQuery("SELECT * FROM " + table);
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

	@ParameterizedTest(name="delete rows")
	@Order(4)
	@MethodSource("getDatabaseConfigs")
	public void testRemoveEntryQuery(final Object config) throws SQLException {
		IDatabaseAdapter adapter = this.reportConfigAndGetAdapter(config);
		String table = this.getTablename(adapter);
		int numEntriesBefore = this.numEntries(adapter, table);
		assertEquals(2, numEntriesBefore);
		adapter.update("DELETE FROM " + table + "");
		int numEntriesAfter = this.numEntries(adapter, table);
		assertEquals("No entry added!", 0, numEntriesAfter);
	}

	@ParameterizedTest(name="drop table")
	@Order(5)
	@MethodSource("getDatabaseConfigs")
	public void testDropTable(final Object config) throws SQLException, IOException {
		IDatabaseAdapter adapter = this.reportConfigAndGetAdapter(config);
		String table = this.getTablename(adapter);
		adapter.update("DROP TABLE " + table);
		assertFalse(adapter.doesTableExist(table));
	}

	public int numEntries(final IDatabaseAdapter adapter, final String table) throws SQLException {
		return adapter.getResultsOfQuery("SELECT COUNT(*) as num FROM " + table).get(0).getAsInt("num");
	}

}
