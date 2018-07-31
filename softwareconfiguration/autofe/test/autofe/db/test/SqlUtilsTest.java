package autofe.db.test;

import static org.junit.Assert.assertEquals;

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

		assertEquals("FE_FWD_CREDIBLE", SqlUtils.getTableNameForFeature(credibleFeature));
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
		path.addPathElement(new BackwardRelationship("TargetTable", "TestTable", "TargetTableId"), AggregationFunction.SUM);
		
		assertEquals("FE_BWD_PRICE_AVGORDERS_TESTTABLE_SUMTARGETTABLE", SqlUtils.getTableNameForFeature(bf));
	}

}
