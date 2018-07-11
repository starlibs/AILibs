package autofe.db.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import autofe.db.sql.SqlStatement;
import autofe.db.util.SqlUtils;

public class SqlUtilsTest {

	@Test
	public void testReplacement() {
		String in = "SELECT * FROM $1";
		String replacement = "Customer";
		String finalStatement = SqlUtils.replacePlaceholder(in, 1, replacement);
		assertEquals("SELECT * FROM Customer", finalStatement);
	}

	@Test
	public void testBackwardStatement() {
		String statement = "CREATE VIEW Orders_VIEW AS SELECT f.*,t.AGG as 'MAX(Product.Price)' FROM Orders_VIEW_TEMP f JOIN (SELECT OrderId,MAX(Price) AS AGG FROM Product_VIEW GROUP BY (OrderId) t ON (f.OrderId = t.OrderId)";

		String rawStatement = SqlStatement.BACKWARD_AGGREGATION;
		String fromViewName = "Orders_VIEW";
		String aggregatedAttributeName = "MAX(Product.Price)";
		String fromViewNameTemp = "Orders_VIEW_TEMP";
		String commonAttribute = "OrderId";
		String aggregationFunction = "MAX";
		String toBeAggregated = "Price";
		String toViewName = "Product_VIEW";

		rawStatement = SqlUtils.replacePlaceholder(rawStatement, 1, fromViewName);
		rawStatement = SqlUtils.replacePlaceholder(rawStatement, 2, aggregatedAttributeName);
		rawStatement = SqlUtils.replacePlaceholder(rawStatement, 3, fromViewNameTemp);
		rawStatement = SqlUtils.replacePlaceholder(rawStatement, 4, commonAttribute);
		rawStatement = SqlUtils.replacePlaceholder(rawStatement, 5, aggregationFunction);
		rawStatement = SqlUtils.replacePlaceholder(rawStatement, 6, toBeAggregated);
		rawStatement = SqlUtils.replacePlaceholder(rawStatement, 7, toViewName);
		
		assertEquals(rawStatement, statement);
	}

}
