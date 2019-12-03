package ai.libs.jaicore.experiments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class QueryBuilderTest {

	public static String insertMultiple(final String tablename, final List<String> keys, final List<List<?>> values) {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("INSERT INTO " + tablename + " (");
		queryBuilder.append(keys.stream().collect(Collectors.joining(",")));
		queryBuilder.append(") VALUES (");
		queryBuilder.append(values.stream().map(x -> "'" + x.stream().map(y -> y + "").collect(Collectors.joining("','")) + "'").collect(Collectors.joining("), (")));
		queryBuilder.append(")");
		return queryBuilder.toString();
	}

	@Test
	public void test() {
		System.out.println(insertMultiple("myTable", Arrays.asList("ka", "kb"), Arrays.asList(Arrays.asList("va1", "vb1"), Arrays.asList("va2", "vb2"))));
	}

}
