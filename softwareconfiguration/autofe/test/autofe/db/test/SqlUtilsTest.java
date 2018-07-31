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

	

}
