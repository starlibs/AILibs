package autofe.db.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import autofe.db.model.database.AbstractAttribute;
import autofe.db.model.database.AggregatedAttribute;
import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.SimpleAttribute;
import autofe.db.model.database.AttributeType;
import autofe.db.model.database.BackwardRelationship;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardRelationship;
import autofe.db.model.database.Table;
import autofe.db.util.DBUtils;

public class SerializationTest {

	private static final String SERIALIZATION_FILE = "temp_db.json";

	private Database db;

	@Before
	public void initDb() {
		db = new Database();

		// Create tables

		SimpleAttribute customerId = new SimpleAttribute("CustomerId", AttributeType.ID);
		SimpleAttribute bankAccountId = new SimpleAttribute("BankAccountId", AttributeType.ID);

		Table bankAccount = new Table();
		bankAccount.setName("BankAccount");
		bankAccount.setTarget(true);
		List<AbstractAttribute> bankAccountAttributes = new ArrayList<>();
		bankAccountAttributes.add(bankAccountId);
		bankAccountAttributes.add(new SimpleAttribute("BankName", AttributeType.TEXT));
		bankAccountAttributes.add(new SimpleAttribute("Credible", AttributeType.NUMERIC, true));
		bankAccount.setColumns(bankAccountAttributes);

		Table customer = new Table();
		customer.setName("Customer");
		customer.setTarget(false);
		List<AbstractAttribute> customerAttributes = new ArrayList<>();
		customerAttributes.add(customerId);
		customerAttributes.add(new SimpleAttribute("FirstName", AttributeType.TEXT));
		customerAttributes.add(bankAccountId);
		customer.setColumns(customerAttributes);

		Table orders = new Table();
		orders.setName("Orders");
		orders.setTarget(false);
		List<AbstractAttribute> ordersAttributes = new ArrayList<>();
		ordersAttributes.add(new SimpleAttribute("OrderId", AttributeType.ID));
		ordersAttributes.add(customerId);
		ordersAttributes.add(new SimpleAttribute("OrderDate", AttributeType.DATE));

		AggregatedAttribute aggregatedAttribute = new AggregatedAttribute("MAX(Product.Price)", AttributeType.NUMERIC,
				new SimpleAttribute("Price", AttributeType.NUMERIC), AggregationFunction.MAX);
		ordersAttributes.add(aggregatedAttribute);
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
		forwardRelationship.setFrom(bankAccount);
		forwardRelationship.setTo(customer);
		forwardRelationship.setCommonAttribute(bankAccountId);
		forwardRelationships.add(forwardRelationship);
		db.setForwards(forwardRelationships);

		// Create backward relationships
		List<BackwardRelationship> backwardRelationships = new ArrayList<>();
		BackwardRelationship backwardRelationship = new BackwardRelationship();
		backwardRelationship.setFrom(customer);
		backwardRelationship.setTo(orders);
		backwardRelationship.setCommonAttribute(customerId);
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
