package ai.libs.jaicore.db.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ai.libs.jaicore.basic.sets.Pair;

public class MySQLQueryBuilder implements ISQLQueryBuilder {

	private static final String DB_DRIVER = "mysql";
	private static final String KEY_EQUALS_VALUE_TO_BE_SET = " = (?)";

	private static final String STR_SPACE_AND = " AND ";
	private static final String STR_SPACE_WHERE = " WHERE ";

	@Override
	public Pair<String, List<Object>> buildInsertStatement(final String table, final Map<String, ? extends Object> map) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		List<Object> values = new ArrayList<>();
		for (Entry<String, ? extends Object> entry : map.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			if (sb1.length() != 0) {
				sb1.append(", ");
				sb2.append(", ");
			}
			sb1.append(entry.getKey());
			sb2.append("?");
			values.add(entry.getValue());
		}
		String statement = "INSERT INTO " + table + " (" + sb1.toString() + ") VALUES (" + sb2.toString() + ")";
		return new Pair<>(statement, values);
	}

	@Override
	public String buildInsertSQLCommand(final String table, final Map<String, ? extends Object> map) {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for (Entry<String, ? extends Object> entry : map.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			if (sb1.length() != 0) {
				sb1.append(", ");
				sb2.append(", ");
			}
			sb1.append(entry.getKey());
			sb2.append("\"" + entry.getValue().toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
		}
		return "INSERT INTO " + table + " (" + sb1.toString() + ") VALUES (" + sb2.toString() + ")";
	}

	@Override
	public String buildMultiInsertSQLCommand(final String table, final List<String> keys, final List<List<?>> datarows) {
		StringBuilder sbMain = new StringBuilder();
		StringBuilder sbKeys = new StringBuilder();
		StringBuilder sbValues = new StringBuilder();

		/* create command phrase */
		sbMain.append("INSERT INTO `");
		sbMain.append(table);
		sbMain.append("` (");

		/* create key phrase */
		for (String key : keys) {
			if (sbKeys.length() != 0) {
				sbKeys.append(", ");
			}
			sbKeys.append(key);
		}
		sbMain.append(sbKeys);
		sbMain.append(") VALUES\n");

		/* create value phrases */
		for (List<?> datarow : datarows) {
			if (datarow.contains(null)) { // the rule that fires here is wrong! The list CAN contain a null element
				throw new IllegalArgumentException("Row " + datarow + " contains null element!");
			}
			if (sbValues.length() > 0) {
				sbValues.append(",\n ");
			}
			sbValues.append("(");
			sbValues.append(datarow.stream().map(s -> "\"" + s.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"").collect(Collectors.joining(", ")));
			sbValues.append(")");
		}
		sbMain.append(sbValues);
		return sbMain.toString();
	}

	@Override
	public String parseSQLCommand(final String sql, final List<?> values) {
		Pattern p = Pattern.compile("\\?");
		Matcher m = p.matcher(sql);
		String modifiedSql = sql;
		int index = 0;
		while (m.find()) {
			modifiedSql = modifiedSql.replaceFirst("\\?", values.get(index).toString());
			index ++;
		}
		return modifiedSql;
	}

	@Override
	public String buildSelectSQLCommand(final String table, final Map<String, String> conditions) {
		StringBuilder conditionSB = new StringBuilder();
		List<String> values = new ArrayList<>();
		for (Entry<String, String> entry : conditions.entrySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(STR_SPACE_AND);
			} else {
				conditionSB.append(STR_SPACE_WHERE);
			}
			conditionSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			values.add(entry.getValue());
		}
		return this.parseSQLCommand("SELECT * FROM `" + table + "`" + conditionSB.toString(), values);
	}

}
