package ai.libs.jaicore.db.sql;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

public class MySQLQueryBuilderTester {

	@Test
	public void test() {
		String sql = "SELECT ? FROM ? WHERE ? = '?'";
		String expected = "SELECT f1, f2 FROM t WHERE c = 'a'";
		ISQLQueryBuilder builder = new MySQLQueryBuilder();
		assertEquals(expected, builder.parseSQLCommand(sql, Arrays.asList("f1, f2", "t", "c", "a")));
	}
}
