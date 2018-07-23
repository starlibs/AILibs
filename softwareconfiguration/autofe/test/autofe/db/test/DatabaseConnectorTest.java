package autofe.db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import autofe.db.model.database.Database;
import autofe.db.model.operation.DatabaseOperation;
import autofe.db.model.operation.ForwardJoinOperation;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.sql.DatabaseConnector;
import autofe.db.sql.DatabaseConnectorImpl;
import autofe.db.sql.DatabaseHelper;
import autofe.db.util.DBUtils;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class DatabaseConnectorTest {

	private static final String MODEL_JSON = "model/db/bankaccount_toy_database.json";

	private static final String JDBC_URL = "jdbc:sqlite:model/db/bankaccount_toy_database.db";

	private Database db;

	private DatabaseConnector dbCon;

	@Before
	public void loadDatabase() {
		this.db = DBUtils.deserializeFromFile(MODEL_JSON);
		db.setJdbcUrl(JDBC_URL);
		this.dbCon = new DatabaseConnectorImpl(db);
	}

	@After
	public void closeConnector() {
		dbCon.close();
	}

	@Test
	public void testGetData() {
		Instances instances = dbCon.getInstances();

		// Check if instances were loaded
		assertNotNull(instances);

		// Check first instance
		Instance i = instances.get(0);
		assertEquals(i.value(0), 100, 0);
		assertEquals(i.value(1), 5, 0);
		Attribute a = i.attribute(2);
		assertEquals(a.value((int) i.value(2)), "Bankinstitut 1");
		assertEquals(i.value(3), 0, 0);
	}

	@Test
	public void testPrepareCleanup() throws SQLException {
		// Load tables before prepare
		List<String> viewNamesBefore = getViewNames();

		// Test prepare
		dbCon.prepareDatabase();
		List<String> viewNames = getViewNames();
		assertTrue(viewNames.contains("BankAccount_VIEW"));
		assertTrue(viewNames.contains("Customer_VIEW"));
		assertTrue(viewNames.contains("Orders_VIEW"));
		assertTrue(viewNames.contains("Product_VIEW"));

		// Test cleanup
		dbCon.cleanup();
		List<String> viewNamesAfter = getViewNames();

		assertEquals(viewNamesBefore, viewNamesAfter);

	}

	private List<String> getViewNames() throws SQLException {
		List<String> toReturn = new ArrayList<>();

		DatabaseHelper dbHelper = new DatabaseHelper(db);
		String sql = "SELECT name FROM sqlite_master WHERE type='view'";
		ResultSet rs = dbHelper.executeSelect(sql);
		while (rs.next()) {
			toReturn.add(rs.getString(1));
		}
		dbHelper.close(rs);
		dbHelper.closeConnection();
		return toReturn;
	}

	@Test
	public void testForwardOperation() throws SQLException {
		// Prepare forward operation
		ForwardRelationship fr = new ForwardRelationship();
		fr.setCommonAttributeName("BankAccountId");
		fr.setFromTableName("BankAccount");
		fr.setToTableName("Customer");
		fr.setContext(db);
		ForwardJoinOperation operation = new ForwardJoinOperation(fr);
		List<DatabaseOperation> operations = new ArrayList<>();
		operations.add(operation);
		db.setOperationHistory(operations);

		// Prepare database
		dbCon.prepareDatabase();

		// Apply forward operation
		dbCon.applyOperations();

		// Load column names
		List<String> columnNames = getColumnNames("BankAccount_VIEW");

		// Cleanup in any case
		dbCon.cleanup();

		// Check columns
		assertEquals(7, columnNames.size());
		assertEquals("BankAccountId", columnNames.get(0));
		assertEquals("Balance", columnNames.get(1));
		assertEquals("TransactionCounter", columnNames.get(2));
		assertEquals("BankName", columnNames.get(3));
		assertEquals("Credible", columnNames.get(4));
		assertEquals("CustomerId", columnNames.get(5));
		assertEquals("FirstName", columnNames.get(6));

	}

	private List<String> getColumnNames(String forTable) throws SQLException {
		List<String> toReturn = new ArrayList<>();

		DatabaseHelper dbHelper = new DatabaseHelper(db);
		String sql = String.format("pragma table_info(%s)", forTable);
		ResultSet rs = dbHelper.executeSelect(sql);
		while (rs.next()) {
			toReturn.add(rs.getString(2));
		}
		dbHelper.close(rs);
		dbHelper.closeConnection();
		return toReturn;
	}

}
