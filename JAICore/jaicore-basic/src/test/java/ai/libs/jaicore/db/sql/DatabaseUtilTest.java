package ai.libs.jaicore.db.sql;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.api4.java.datastructure.kvstore.IKVStore;
import org.junit.Test;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.db.DatabaseUtil;

public class DatabaseUtilTest {

	private static final SQLAdapter ADAPTER = new SQLAdapter("localhost", "test", "test", "test");

	@Test
	public void testTransfer() throws SQLException {
		Map<String, Pair<Class<?>, Function<IKVStore, Object>>> transformations = new HashMap<>();
		DatabaseUtil.createTableFromResult(ADAPTER, "SELECT * FROM testtable", Arrays.asList(), "newtable", Arrays.asList("a", "b"), transformations);
	}
}
