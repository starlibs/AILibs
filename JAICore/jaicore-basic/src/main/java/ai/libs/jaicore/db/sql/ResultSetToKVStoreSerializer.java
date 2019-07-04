package ai.libs.jaicore.db.sql;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import ai.libs.jaicore.basic.kvstore.IKVStore;
import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.basic.kvstore.KVStoreCollection;

public class ResultSetToKVStoreSerializer {

	public List<IKVStore> serialize(final ResultSet values) throws IOException {
		try {
			ResultSetMetaData rsmd = values.getMetaData();
			int numColumns = rsmd.getColumnCount();
			String[] columnNames = new String[numColumns];
			int[] columnTypes = new int[numColumns];

			for (int i = 0; i < columnNames.length; i++) {
				columnNames[i] = rsmd.getColumnLabel(i + 1);
				columnTypes[i] = rsmd.getColumnType(i + 1);
			}

			KVStoreCollection collection = new KVStoreCollection();

			while (values.next()) {
				KVStore store = new KVStore();

				for (int i = 0; i < columnNames.length; i++) {
					String fieldName = columnNames[i];

					switch (columnTypes[i]) {
					case Types.INTEGER:
						store.put(fieldName, values.getInt(i + 1));
						break;
					case Types.BIGINT:
						store.put(fieldName, values.getLong(i + 1));
						break;
					case Types.DECIMAL:
					case Types.NUMERIC:
						store.put(fieldName, values.getBigDecimal(i + 1));
						break;
					case Types.FLOAT:
					case Types.REAL:
					case Types.DOUBLE:
						store.put(fieldName, values.getDouble(i + 1));
						break;
					case Types.NVARCHAR:
					case Types.VARCHAR:
					case Types.LONGNVARCHAR:
					case Types.LONGVARCHAR:
						store.put(fieldName, values.getString(i + 1));
						break;
					case Types.BOOLEAN:
					case Types.BIT:
						store.put(fieldName, values.getBoolean(i + 1));
						break;
					case Types.BINARY:
					case Types.VARBINARY:
					case Types.LONGVARBINARY:
						store.put(fieldName, values.getByte(i + 1));
						break;
					case Types.TINYINT:
					case Types.SMALLINT:
						store.put(fieldName, values.getShort(i + 1));
						break;
					case Types.DATE:
						store.put(fieldName, values.getDate(i + 1));
						break;
					case Types.TIMESTAMP:
						store.put(fieldName, values.getTime(i + 1));
						break;

					case Types.BLOB:
						store.put(fieldName, values.getBlob(i));
						break;

					case Types.CLOB:
						store.put(fieldName, values.getClob(i));
						break;

					case Types.ARRAY:
						throw new ResultSetSerializerException("ResultSetSerializer not yet implemented for SQL type ARRAY");

					case Types.STRUCT:
						throw new ResultSetSerializerException("ResultSetSerializer not yet implemented for SQL type STRUCT");

					case Types.DISTINCT:
						throw new ResultSetSerializerException("ResultSetSerializer not yet implemented for SQL type DISTINCT");

					case Types.REF:
						throw new ResultSetSerializerException("ResultSetSerializer not yet implemented for SQL type REF");

					case Types.JAVA_OBJECT:
					default:
						store.put(fieldName, values.getObject(i + 1));
						break;
					}

					if (values.wasNull()) {
						store.put(fieldName, null);
					}
				}

				collection.add(store);
			}

			return collection;
		} catch (SQLException e) {
			throw new ResultSetSerializerException(e);
		}

	}

}
