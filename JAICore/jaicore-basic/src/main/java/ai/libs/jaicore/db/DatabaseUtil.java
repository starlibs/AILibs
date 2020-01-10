package ai.libs.jaicore.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.api4.java.datastructure.kvstore.IKVStore;

import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.db.sql.SQLAdapter;

public class DatabaseUtil {

	private DatabaseUtil() {
		/* avoids instantiation */
	}

	public static void createTableFromResult(final SQLAdapter adapter, final String sqlQuery, final List<String> params, final String tableName, final List<String> fieldnames, final Map<String, Pair<Class<?>, Function<IKVStore, Object>>> transformation) throws SQLException {

		if (fieldnames.size() != transformation.size() || !fieldnames.containsAll(transformation.keySet())) {
			throw new IllegalArgumentException("Fieldnames is " + fieldnames + " but keys in transformation table are " + transformation.keySet());
		}

		/* create new table */
		Map<String, String> typeMap = new HashMap<>();
		List<String> fields = new ArrayList<>();
		for (Entry<String, Pair<Class<?>, Function<IKVStore, Object>>> key : transformation.entrySet()) {
			fields.add(key.getKey());
			String type = null;
			switch (key.getValue().getX().getName()) {
			case "java.lang.Double":
				type = "DOUBLE";
				break;
			case "java.lang.String":
				type = "TEXT";
				break;
			default:
				throw new UnsupportedOperationException("Target class " + key.getValue().getX().getName() + " not supported.");
			}
			typeMap.put(key.getKey(), type);
		}
		typeMap.put("c_id", "INT(8)");
		adapter.createTable(tableName, "c_id", fields, typeMap, Arrays.asList());

		/* transfer data in chunks */
		int pageSize = 100;
		Set<String> keys = transformation.keySet();
		boolean insertedLine = true;
		for (int page = 0; insertedLine; page ++) {
			String qry = sqlQuery + " LIMIT " + (pageSize * page) + ", " + pageSize;
			Iterator<IKVStore> rowIterator = adapter.getResultIteratorOfQuery(qry, params);
			insertedLine = false;
			while (rowIterator.hasNext()) {
				IKVStore row = rowIterator.next();

				/* convert row */
				Map<String, Object> tRow = new HashMap<>();
				for (String key : keys) {
					tRow.put(key, transformation.get(key).getY().apply(row));
				}
				adapter.insert(tableName, tRow);
				insertedLine = true;
			}
		}
	}

}
