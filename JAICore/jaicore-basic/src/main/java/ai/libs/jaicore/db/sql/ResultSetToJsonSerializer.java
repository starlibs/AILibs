package ai.libs.jaicore.db.sql;

import java.io.IOException;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ResultSetToJsonSerializer extends JsonSerializer<ResultSet> {

	@Override
	public Class<ResultSet> handledType() {
		return ResultSet.class;
	}

	@Override
	public void serialize(final ResultSet values, final JsonGenerator gen, final SerializerProvider serializer) throws IOException {
		try {
			ResultSetMetaData rsmd = values.getMetaData();
			int numColumns = rsmd.getColumnCount();
			String[] columnNames = new String[numColumns];
			int[] columnTypes = new int[numColumns];

			for (int i = 0; i < columnNames.length; i++) {
				columnNames[i] = rsmd.getColumnLabel(i + 1);
				columnTypes[i] = rsmd.getColumnType(i + 1);
			}

			gen.writeStartArray();

			while (values.next()) {
				boolean b;
				long l;
				double d;

				gen.writeStartObject();

				for (int i = 0; i < columnNames.length; i++) {

					gen.writeFieldName(columnNames[i]);
					switch (columnTypes[i]) {

					case Types.INTEGER:
						l = values.getInt(i + 1);
						if (values.wasNull()) {
							gen.writeNull();
						} else {
							gen.writeNumber(l);
						}
						break;

					case Types.BIGINT:
						l = values.getLong(i + 1);
						if (values.wasNull()) {
							gen.writeNull();
						} else {
							gen.writeNumber(l);
						}
						break;

					case Types.DECIMAL:
					case Types.NUMERIC:
						gen.writeNumber(values.getBigDecimal(i + 1));
						break;

					case Types.FLOAT:
					case Types.REAL:
					case Types.DOUBLE:
						d = values.getDouble(i + 1);
						if (values.wasNull()) {
							gen.writeNull();
						} else {
							gen.writeNumber(d);
						}
						break;

					case Types.NVARCHAR:
					case Types.VARCHAR:
					case Types.LONGNVARCHAR:
					case Types.LONGVARCHAR:
						gen.writeString(values.getString(i + 1));
						break;

					case Types.BOOLEAN:
					case Types.BIT:
						b = values.getBoolean(i + 1);
						if (values.wasNull()) {
							gen.writeNull();
						} else {
							gen.writeBoolean(b);
						}
						break;

					case Types.BINARY:
					case Types.VARBINARY:
					case Types.LONGVARBINARY:
						gen.writeBinary(values.getBytes(i + 1));
						break;

					case Types.TINYINT:
					case Types.SMALLINT:
						l = values.getShort(i + 1);
						if (values.wasNull()) {
							gen.writeNull();
						} else {
							gen.writeNumber(l);
						}
						break;

					case Types.DATE:
						serializer.defaultSerializeDateValue(values.getDate(i + 1), gen);
						break;

					case Types.TIMESTAMP:
						serializer.defaultSerializeDateValue(values.getTime(i + 1), gen);
						break;

					case Types.BLOB:
						Blob blob = values.getBlob(i);
						serializer.defaultSerializeValue(blob.getBinaryStream(), gen);
						blob.free();
						break;

					case Types.CLOB:
						Clob clob = values.getClob(i);
						serializer.defaultSerializeValue(clob.getCharacterStream(), gen);
						clob.free();
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
						serializer.defaultSerializeValue(values.getObject(i + 1), gen);
						break;
					}
				}

				gen.writeEndObject();
			}

			gen.writeEndArray();

		} catch (SQLException e) {
			throw new ResultSetSerializerException(e);
		}

	}

}
