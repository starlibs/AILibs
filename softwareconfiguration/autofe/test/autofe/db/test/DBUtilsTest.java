package autofe.db.test;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import autofe.db.model.database.Database;
import autofe.db.model.database.Table;
import autofe.db.util.DBUtils;

public class DBUtilsTest {

	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	private Database db;

	@Before
	public void loadDb() {
		db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
	}

	@Test
	public void testForwardReachableTables() {
		Set<Table> expected = new HashSet<>();
		expected.add(DBUtils.getTableByName("Customer", db));
		expected.add(DBUtils.getTableByName("BankAccount", db));

		Table from = DBUtils.getTableByName("BankAccount", db);
		Set<Table> actual = DBUtils.getForwardReachableTables(from, db);

		assertEquals(expected, actual);
	}
	
	@Test
	public void testBackwardReachableTables() {
		Set<Table> expected = new HashSet<>();
		expected.add(DBUtils.getTableByName("Orders", db));
		expected.add(DBUtils.getTableByName("Product", db));
		
		Table from = DBUtils.getTableByName("BankAccount", db);
		Set<Table> actual = DBUtils.getBackwardReachableTables(from, db);

		assertEquals(expected, actual);
	}

}
