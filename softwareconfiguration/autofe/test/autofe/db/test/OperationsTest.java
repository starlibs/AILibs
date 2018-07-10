package autofe.db.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import autofe.db.model.AggregatedAttribute;
import autofe.db.model.AggregationFunction;
import autofe.db.model.Attribute;
import autofe.db.model.AttributeType;
import autofe.db.model.BackwardAggregateOperation;
import autofe.db.model.BackwardRelationship;
import autofe.db.model.Database;
import autofe.db.model.ForwardJoinOperation;
import autofe.db.model.ForwardRelationship;
import autofe.db.model.Table;
import autofe.db.util.DBUtils;

public class OperationsTest {
	
	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";
	
	private Database db;
	
	@Before
	public void loadDatabase() {
		db = DBUtils.deserialize(DATABASE_MODEL_FILE);
	}
	
	@Test
	public void testBackwardOperationsCustomer() {
		// Define possible backward operations manually
		BackwardRelationship br = new BackwardRelationship();
		Table customer = DBUtils.getTableByName("Customer", db);
		Table orders = DBUtils.getTableByName("Orders", db);
		Table product = DBUtils.getTableByName("Product", db);
		br.setFrom(orders);
		br.setTo(product);
		br.setCommonAttribute(DBUtils.getAttributeByName("OrderId", orders));
		
		Attribute price = DBUtils.getAttributeByName("Price", product);
		
		Set<BackwardAggregateOperation> operations = new HashSet<>();
		operations.add(new BackwardAggregateOperation(br, AggregationFunction.AVG, price));
		operations.add(new BackwardAggregateOperation(br, AggregationFunction.MAX, price));
		operations.add(new BackwardAggregateOperation(br, AggregationFunction.MIN, price));
		operations.add(new BackwardAggregateOperation(br, AggregationFunction.SUM, price));
		
		Set<BackwardAggregateOperation> computedOperations = DBUtils.getBackwardAggregateOperations(customer, db);
		assertEquals(operations, computedOperations);
	}
	
	@Test
	public void testBackwardOperationsBankAccount() {
		// No backward operations possible from table bankaccount
		BackwardRelationship br = new BackwardRelationship();
		Table bankAccount = DBUtils.getTableByName("BankAccount", db);
		Set<BackwardAggregateOperation> computedOperations = DBUtils.getBackwardAggregateOperations(bankAccount, db);
		assertTrue(computedOperations.isEmpty());
	}
	
	@Test
	public void testForwardOperationsBankAccount() {
		ForwardRelationship fr = new ForwardRelationship();
		Table customer = DBUtils.getTableByName("Customer", db);
		Table bankAccount = DBUtils.getTableByName("BankAccount", db);
		Attribute bankAccountId = DBUtils.getAttributeByName("BankAccountId", customer);
		fr.setFrom(bankAccount);
		fr.setTo(customer);
		fr.setCommonAttribute(bankAccountId);
		
		Set<ForwardJoinOperation> operations = new HashSet<>();
		operations.add(new ForwardJoinOperation(fr));
		
		Set<ForwardJoinOperation> computedOperations = DBUtils.getForwardJoinOperations(bankAccount, db);
		assertEquals(operations,computedOperations);
	}
	
	@Test
	public void testApplyBackwardOperation() {
		//Construct state after operations manually
		Database modifiedDb = applyBackwardOperationManually();
				
		BackwardRelationship br = new BackwardRelationship();
		Table orders = DBUtils.getTableByName("Orders", db);
		Table product = DBUtils.getTableByName("Product", db);
		br.setFrom(orders);
		br.setTo(product);
		br.setCommonAttribute(DBUtils.getAttributeByName("OrderId", orders));	
		Attribute price = DBUtils.getAttributeByName("Price", product);
		BackwardAggregateOperation operation = new BackwardAggregateOperation(br, AggregationFunction.MAX, price);
		
		operation.applyTo(db);
		
		assertEquals(modifiedDb,db);
	}
	
	private Database applyBackwardOperationManually() {
		Database modifiedDb = DBUtils.deserialize(DATABASE_MODEL_FILE);
		Table orders = DBUtils.getTableByName("Orders", modifiedDb);
		Table product = DBUtils.getTableByName("Product", modifiedDb);
		Attribute price = DBUtils.getAttributeByName("Price", product);
		AggregatedAttribute aggregatedAttribute = new AggregatedAttribute("MAX(Product.Price)", AttributeType.NUMERIC, price, AggregationFunction.MAX);
		orders.getColumns().add(aggregatedAttribute);
		
		BackwardRelationship br = new BackwardRelationship();
		br.setFrom(orders);
		br.setTo(product);
		br.setCommonAttribute(DBUtils.getAttributeByName("OrderId", orders));	
		BackwardAggregateOperation operation = new BackwardAggregateOperation(br, AggregationFunction.MAX, price);
		
		modifiedDb.getOperationHistory().add(operation);
		
		return modifiedDb;
	}

}
