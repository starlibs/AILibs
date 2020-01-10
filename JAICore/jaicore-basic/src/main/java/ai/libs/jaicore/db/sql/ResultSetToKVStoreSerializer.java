package ai.libs.jaicore.db.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.api4.java.datastructure.kvstore.IKVStore;

import ai.libs.jaicore.basic.kvstore.KVStore;
import ai.libs.jaicore.basic.kvstore.KVStoreCollection;
import ai.libs.jaicore.logging.LoggerUtil;

public class ResultSetToKVStoreSerializer {

	public List<IKVStore> serialize(final ResultSet values) {
		KVStoreCollection collection = new KVStoreCollection();
		Iterator<IKVStore> it = this.getSerializationIterator(values);
		while (it.hasNext()) {
			collection.add(it.next());
		}
		return collection;
	}

	public Iterator<IKVStore> getSerializationIterator(final ResultSet values) {
		return new Iterator<IKVStore>() {

			private boolean init = false;
			private boolean hasNext = false;
			private String[] columnNames;
			private int[] columnTypes;

			private void init() throws SQLException {
				if (!this.init) {
					this.init = true;
					ResultSetMetaData rsmd = values.getMetaData();
					int numColumns = rsmd.getColumnCount();
					this.hasNext = values.next();
					this.columnNames = new String[numColumns];
					this.columnTypes = new int[numColumns];
					for (int i = 0; i < this.columnNames.length; i++) {
						this.columnNames[i] = rsmd.getColumnLabel(i + 1);
						this.columnTypes[i] = rsmd.getColumnType(i + 1);
					}
				}
			}

			@Override
			public boolean hasNext() {
				try {
					this.init();
					return this.hasNext;
				} catch (SQLException e) {
					throw new NoSuchElementException(LoggerUtil.getExceptionInfo(e));
				}
			}

			@Override
			public IKVStore next() {
				try {
					this.init();
					IKVStore val = ResultSetToKVStoreSerializer.this.serializeRow(values, this.columnNames, this.columnTypes);
					this.hasNext = values.next();
					if (!this.hasNext) {
						values.getStatement().close();
					}
					return val;
				} catch (ResultSetSerializerException | SQLException e) {
					throw new NoSuchElementException(LoggerUtil.getExceptionInfo(e));
				}
			}
		};
	}

	public IKVStore serializeRow(final ResultSet values, final String[] columnNames, final int[] columnTypes) throws SQLException, ResultSetSerializerException {
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
		return store;
	}

}
