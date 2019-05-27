package autofe.db.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import autofe.db.model.database.AggregationFunction;
import autofe.db.model.database.Attribute;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.model.database.ForwardFeature;
import autofe.db.model.database.Path;
import autofe.db.model.database.Table;
import autofe.db.model.relation.BackwardRelationship;
import autofe.db.model.relation.ForwardRelationship;
import autofe.db.util.DBUtils;
import autofe.db.util.SqlUtils;

public class SqlUtilsTest {

	private static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	@Test
	public void testReplacement() {
		String in = "SELECT * FROM $1";
		String replacement = "Customer";
		String finalStatement = SqlUtils.replacePlaceholder(in, 1, replacement);
		assertEquals("SELECT * FROM Customer", finalStatement);
	}

	@Test
	public void testForwardFeatureName() {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		Table bankAccount = DBUtils.getTableByName("BankAccount", db);
		Attribute credible = DBUtils.getAttributeByName("Credible", bankAccount);
		ForwardFeature credibleFeature = new ForwardFeature(credible);

		assertEquals("FE_FWD_BANKACCOUNT_CREDIBLE", SqlUtils.getTableNameForFeature(credibleFeature));
	}

	@Test
	public void testBackwardFeatureName() {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		Table product = DBUtils.getTableByName("Product", db);
		Attribute price = DBUtils.getAttributeByName("Price", product);
		BackwardFeature bf = new BackwardFeature(price);
		Path path = bf.getPath();
		path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.AVG);
		path.addPathElement(new ForwardRelationship("TestTable", "Orders", "TestTableId"), null);
		path.addPathElement(new BackwardRelationship("TargetTable", "TestTable", "TargetTableId"),
				AggregationFunction.SUM);

		assertEquals("FE_BWD_PRODUCT_PRICE_AVGORDERS_TESTTABLE_SUMTARGETTABLE", SqlUtils.getTableNameForFeature(bf));
	}

	@Test
	public void testForwardFeatureSql() {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		Table customer = DBUtils.getTableByName("Customer", db);
		Attribute firstName = DBUtils.getAttributeByName("FirstName", customer);
		ForwardFeature credibleFeature = new ForwardFeature(firstName);

		List<ForwardRelationship> joinPath = Collections
				.singletonList(new ForwardRelationship("BankAccount", "Customer", "BankAccountId"));

		String expected = "SELECT `BankAccount`.`BankAccountId`, `Customer`.`FirstName` FROM `BankAccount` JOIN `Customer` ON (`Customer`.`BankAccountId` = `BankAccount`.`BankAccountId`)";

		assertEquals(expected, SqlUtils.generateForwardSql(joinPath, credibleFeature, db));

	}

	@Test
	public void testBackwardFeatureSql() {
		Database db = DBUtils.deserializeFromFile(DATABASE_MODEL_FILE);
		Table product = DBUtils.getTableByName("Product", db);
		Attribute price = DBUtils.getAttributeByName("Price", product);
		BackwardFeature feature = new BackwardFeature(price);
		Path path = feature.getPath();
		path.addPathElement(new BackwardRelationship("Orders", "Product", "OrderId"), AggregationFunction.AVG);
		path.addPathElement(new BackwardRelationship("Customer", "Orders", "CustomerId"), AggregationFunction.SUM);

		List<ForwardRelationship> joinPath = Collections
				.singletonList(new ForwardRelationship("BankAccount", "Customer", "BankAccountId"));

		String expected = "SELECT `TEMPFEATURE1` AS 'BankAccount.(Customer.SUM(Orders.AVG(Product.Price)))', `BankAccount`.`BankAccountId` FROM `BankAccount` LEFT OUTER JOIN (SELECT `Customer`.`CustomerId`, `Customer`.`BankAccountId`, SUM(`TEMPFEATURE0`) AS TEMPFEATURE1 FROM `Customer` LEFT OUTER JOIN (SELECT `Orders`.`OrderId`, `Orders`.`CustomerId`, AVG(`Product`.`Price`) AS TEMPFEATURE0 FROM `Orders` LEFT OUTER JOIN `Product` ON (`Orders`.`OrderId` = `Product`.`OrderId`) GROUP BY `Orders`.`OrderId`) TEMPTABLE1 ON (`Customer`.`CustomerId` = `TEMPTABLE1`.`CustomerId`) GROUP BY `Customer`.`CustomerId`) TEMPTABLE2 ON (`BankAccount`.`BankAccountId` = `TEMPTABLE2`.`BankAccountId`)";
		String actual = SqlUtils.generateBackwardSql(joinPath, feature, db);
		System.out.println(actual);

		assertEquals(expected, actual);
	}

}
