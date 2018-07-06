package autofe.db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import autofe.db.model.Attribute;
import autofe.db.model.AttributeType;
import autofe.db.model.BackwardRelationship;
import autofe.db.model.Database;
import autofe.db.model.ForwardRelationship;
import autofe.db.model.Table;
import autofe.db.util.DBUtils;

public class SerializationTest {
	
	private static final String SERIALIZATION_FILE = "temp_db.json";

	private Database db;

	@Before
	public void initDb() {
		db = new Database();

		// Create tables

		Attribute customerId = new Attribute("CustomerId", AttributeType.ID);
		Attribute bankAccountId = new Attribute("BankAccountId", AttributeType.ID);

		Table bankAccount = new Table();
		bankAccount.setName("BankAccount");
		bankAccount.setTarget(true);
		Set<Attribute> bankAccountAttributes = new HashSet<>();
		bankAccountAttributes.add(bankAccountId);
		bankAccountAttributes.add(new Attribute("BankName", AttributeType.TEXT));
		bankAccountAttributes.add(new Attribute("Credible", AttributeType.NUMERIC, true));
		bankAccount.setColumns(bankAccountAttributes);

		Table customer = new Table();
		customer.setName("Customer");
		customer.setTarget(false);
		Set<Attribute> customerAttributes = new HashSet<>();
		customerAttributes.add(customerId);
		customerAttributes.add(new Attribute("FirstName", AttributeType.TEXT));
		customerAttributes.add(bankAccountId);
		customer.setColumns(customerAttributes);

		Table orders = new Table();
		orders.setName("Orders");
		orders.setTarget(false);
		Set<Attribute> ordersAttributes = new HashSet<>();
		ordersAttributes.add(new Attribute("OrderId", AttributeType.ID));
		ordersAttributes.add(customerId);
		ordersAttributes.add(new Attribute("OrderDate", AttributeType.DATE));
		orders.setColumns(ordersAttributes);

		// Add tables to DB
		Set<Table> tables = new HashSet<>();
		tables.add(bankAccount);
		tables.add(customer);
		tables.add(orders);
		db.setTables(tables);

		// Create forward relationship
		Set<ForwardRelationship> forwardRelationships = new HashSet<>();
		ForwardRelationship forwardRelationship = new ForwardRelationship();
		forwardRelationship.setFrom(bankAccount);
		forwardRelationship.setTo(customer);
		forwardRelationship.setCommonAttribute(bankAccountId);
		forwardRelationships.add(forwardRelationship);
		db.setForwards(forwardRelationships);

		// Create backward relationships
		Set<BackwardRelationship> backwardRelationships = new HashSet<>();
		BackwardRelationship backwardRelationship = new BackwardRelationship();
		backwardRelationship.setFrom(customer);
		backwardRelationship.setTo(orders);
		backwardRelationship.setCommonAttribute(customerId);
		backwardRelationships.add(backwardRelationship);
		db.setBackwards(backwardRelationships);
	}

	@Test
	public void testSerialization() {
		DBUtils.serialize(db, SERIALIZATION_FILE);
		Database loadedDb = DBUtils.deserialize(SERIALIZATION_FILE);
		assertEquals(db, loadedDb);
	}

}
