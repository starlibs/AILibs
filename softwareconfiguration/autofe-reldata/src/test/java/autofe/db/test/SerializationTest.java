package autofe.db.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import autofe.db.model.database.Attribute;
import autofe.db.model.database.AttributeType;
import autofe.db.model.database.Database;
import autofe.db.model.database.Table;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.DBUtils;

public class SerializationTest {

	private static final String SERIALIZATION_FILE = "temp_db.json";

	private static final String TOY_DATABASE_FILE = "model/db/bankaccount_toy_database.json";

	private Database db;

	@Before
	public void initDb() {
		db = new Database();

		// Create tables

		Attribute customerId = new Attribute("Customer.CustomerId", AttributeType.ID);
		Attribute bankAccountId = new Attribute("BankAccount.BankAccountId", AttributeType.ID);

		Table bankAccount = new Table();
		bankAccount.setName("BankAccount");
		bankAccount.setTarget(true);
		List<Attribute> bankAccountAttributes = new ArrayList<>();
		bankAccountAttributes.add(bankAccountId);
		bankAccountAttributes.add(new Attribute("BankAccount.BankName", AttributeType.TEXT));
		bankAccountAttributes.add(new Attribute("BankAccount.Credible", AttributeType.NUMERIC, true));
		bankAccount.setColumns(bankAccountAttributes);

		Table customer = new Table();
		customer.setName("Customer");
		customer.setTarget(false);
		List<Attribute> customerAttributes = new ArrayList<>();
		customerAttributes.add(customerId);
		customerAttributes.add(new Attribute("Customer.FirstName", AttributeType.TEXT));
		customerAttributes.add(bankAccountId);
		customer.setColumns(customerAttributes);

		Table orders = new Table();
		orders.setName("Orders");
		orders.setTarget(false);
		List<Attribute> ordersAttributes = new ArrayList<>();
		ordersAttributes.add(new Attribute("Orders.OrderId", AttributeType.ID));
		ordersAttributes.add(customerId);
		ordersAttributes.add(new Attribute("Orders.OrderDate", AttributeType.DATE));
		orders.setColumns(ordersAttributes);

		// Add tables to DB
		List<Table> tables = new ArrayList<>();
		tables.add(bankAccount);
		tables.add(customer);
		tables.add(orders);
		db.setTables(tables);

		// Create forward relationship
		List<ForwardRelationship> forwardRelationships = new ArrayList<>();
		ForwardRelationship forwardRelationship = new ForwardRelationship();
		forwardRelationship.setFromTableName(bankAccount.getName());
		forwardRelationship.setToTableName(customer.getName());
		forwardRelationship.setCommonAttributeName(bankAccountId.getName());
		forwardRelationships.add(forwardRelationship);
		db.setForwards(forwardRelationships);

		// Create backward relationships
		List<BackwardRelationship> backwardRelationships = new ArrayList<>();
		BackwardRelationship backwardRelationship = new BackwardRelationship();
		backwardRelationship.setFromTableName(customer.getName());
		backwardRelationship.setToTableName(orders.getName());
		backwardRelationship.setCommonAttributeName(customerId.getName());
		backwardRelationships.add(backwardRelationship);
		db.setBackwards(backwardRelationships);
	}

	@Test
	public void testSerialization() {
		DBUtils.serializeToFile(db, SERIALIZATION_FILE);
		Database loadedDb = DBUtils.deserializeFromFile(SERIALIZATION_FILE);
		assertEquals(db, loadedDb);
		// Delete file
		File toDelete = new File(SERIALIZATION_FILE);
		toDelete.delete();
	}

}
