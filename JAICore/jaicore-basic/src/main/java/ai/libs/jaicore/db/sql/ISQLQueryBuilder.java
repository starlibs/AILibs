package ai.libs.jaicore.db.sql;

import java.util.List;
import java.util.Map;

import ai.libs.jaicore.basic.sets.Pair;

public interface ISQLQueryBuilder {

	public Pair<String, List<Object>> buildInsertStatement(final String table, final Map<String, ? extends Object> values);

	public String parseSQLCommand(final String sql, final List<?> values);

	public String buildInsertSQLCommand(final String table, final Map<String, ? extends Object> values);

	public String buildMultiInsertSQLCommand(final String table, final List<String> keys, final List<List<?>> datarows);

	public String buildSelectSQLCommand(String table, final Map<String, String> conditions);
}
