package ai.libs.jaicore.db.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.kvstore.KVStoreUtil;
import ai.libs.jaicore.db.IDatabaseAdapter;

/**
 * This is a simple util class for easy database access and query execution in sql. You need to make sure that the respective JDBC connector is in the class path. By default, the adapter uses the mysql driver, but any jdbc driver can be
 * used.
 *
 * @author fmohr, mwever
 *
 */
@SuppressWarnings("serial")
class RestSqlAdapter implements IDatabaseAdapter {

	private static final String KEY_EQUALS_VALUE_TO_BE_SET = " = (?)";
	private static final String STR_SPACE_AND = " AND ";
	private static final String STR_SPACE_WHERE = " WHERE ";

	private final transient ISQLQueryBuilder queryBuilder = new MySQLQueryBuilder();
	private transient Logger logger = LoggerFactory.getLogger(RestSqlAdapter.class);
	private final String host;
	private final String token;
	private final String querySuffix;
	private final String selectSuffix;
	private final String insertSuffix;
	private final String updateSuffix;

	public RestSqlAdapter(final IRestDatabaseConfig config) {
		this.host = config.getHost();
		this.token = config.getToken();
		this.querySuffix = config.getQuerySuffix();
		this.selectSuffix = config.getSelectSuffix();
		this.updateSuffix = config.getUpdateSuffix();
		this.insertSuffix = config.getInsertSuffix();
		Objects.requireNonNull(this.host);
		Objects.requireNonNull(this.insertSuffix);
		Objects.requireNonNull(this.updateSuffix);
		Objects.requireNonNull(this.selectSuffix);
		Objects.requireNonNull(this.querySuffix);
	}

	public List<IKVStore> select(final String query) throws SQLException {
		this.logger.info("Sending query {}", query);
		JsonNode res = this.executeRESTCall(this.host + this.selectSuffix, query);
		this.logger.info("Received result as JSON node: {}.", res);
		return KVStoreUtil.readFromJson(res);
	}

	@Override
	public int[] insert(final String table, final Map<String, ? extends Object> values) throws SQLException {
		return this.insert(this.queryBuilder.buildInsertSQLCommand(table, values));
	}

	@Override
	public int[] insertMultiple(final String tablename, final List<String> keys, final List<List<?>> values) throws SQLException {
		return this.insert(this.queryBuilder.buildMultiInsertSQLCommand(tablename, keys, values));
	}

	@Override
	public int[] insert(final String query) throws SQLException {
		JsonNode res = this.executeRESTCall(this.host + this.insertSuffix, query);
		if (res instanceof ArrayNode) {
			ArrayNode array = (ArrayNode) res;
			return IntStream.range(0, array.size()).map(i -> array.get(i).asInt()).toArray();
		} else {
			if ((res.get("status").asInt() == 500) && (res.get("message").textValue().matches("(.*)Duplicate entry(.*) for key(.*)"))) {
				throw new SQLException(res.get("message").textValue());
			}
			throw new IllegalStateException("Cannot parse result for insert query. Result is:\n" + res);
		}
	}

	@Override
	public int update(final String query) throws SQLException {
		JsonNode res = this.executeRESTCall(this.host + this.updateSuffix, query);
		return res.asInt();
	}

	@Override
	public List<IKVStore> query(final String query) throws SQLException {
		JsonNode res = this.executeRESTCall(this.host + this.querySuffix, query);
		return KVStoreUtil.readFromJson(res);
	}

	public JsonNode executeRESTCall(final String URL, final String query) throws SQLException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode root = mapper.createObjectNode();
			root.set("token", root.textNode(this.token));
			root.set("query", root.textNode(query));
			this.logger.info("Sending query {}", query);
			String jsonPayload = mapper.writeValueAsString(root);
			StringEntity requestEntity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
			HttpPost post = new HttpPost(URL);
			post.setHeader("Content-Type", "application/json");
			post.setEntity(requestEntity);
			this.logger.info("Waiting for response.");
			CloseableHttpResponse response = client.execute(post);
			this.logger.info("Received response. Now processing the result.");
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode / 100 == 4 || statusCode / 100 == 5) {
				// status code is 4xx or 5xx
				String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				this.logger.error("SQLasRESTServer returned status code: {}." + " \nThe sql query was: {}." + " \nThe response body is: {}", statusCode, query, responseBody);
				throw new SQLException("SQL Server error: " + responseBody);
			}
			assert (statusCode == HttpStatus.SC_OK);
			HttpEntity entity = response.getEntity();
			return mapper.readTree(entity.getContent());
		} catch (UnsupportedOperationException | IOException e) {
			throw new SQLException(e);
		}
	}

	@Override
	public int update(final String tablename, final Map<String, ? extends Object> valuesToWrite, final Map<String, ? extends Object> where) throws SQLException {
		StringBuilder queryStringBuilder = new StringBuilder();
		queryStringBuilder.append("UPDATE " + tablename + " SET ");
		queryStringBuilder.append(valuesToWrite.entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(Collectors.joining(",")));
		if (!where.isEmpty()) {
			queryStringBuilder.append(STR_SPACE_WHERE);
			queryStringBuilder.append(where.entrySet().stream().map(e -> RestSqlAdapter.whereClauseElement(e.getKey(), e.getValue() != null ? e.getValue().toString() : null)).collect(Collectors.joining(STR_SPACE_AND)));
		}
		return this.update(queryStringBuilder.toString());
	}

	public static String whereClauseElement(final String key, final String value) {
		StringBuilder sb = new StringBuilder();
		sb.append(key);
		if (value == null || value.equals("null")) {
			sb.append(" IS NULL");
		} else {
			sb.append("='");
			sb.append(value);
			sb.append("'");
		}
		return sb.toString();
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void checkConnection() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void createTable(final String tablename, final String nameOfPrimaryField, final Collection<String> fieldnames, final Map<String, String> types, final Collection<String> keys) throws SQLException {
		StringBuilder sqlMainTable = new StringBuilder();
		StringBuilder keyFieldsSB = new StringBuilder();
		sqlMainTable.append("CREATE TABLE IF NOT EXISTS `" + tablename + "` (");
		if (!types.containsKey(nameOfPrimaryField)) {
			throw new IllegalArgumentException("Type for primary field " + nameOfPrimaryField + " not specified.");
		}
		sqlMainTable.append("`" + nameOfPrimaryField + "` " + types.get(nameOfPrimaryField) + " NOT NULL AUTO_INCREMENT,");
		for (String key : fieldnames) {
			sqlMainTable.append("`" + key + "` " + types.get(key) + " NULL,");
			keyFieldsSB.append("`" + key + "`,");
		}
		sqlMainTable.append("PRIMARY KEY (`" + nameOfPrimaryField + "`)");
		sqlMainTable.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin");
		this.update(sqlMainTable.toString());
	}

	@Override
	public List<IKVStore> getRowsOfTable(final String table, final Map<String, String> conditions) throws SQLException {
		return this.query(this.queryBuilder.buildSelectSQLCommand(table, conditions));
	}

	@Override
	public List<IKVStore> getResultsOfQuery(final String query, final List<String> values) throws SQLException {
		if (values.isEmpty()) {
			return this.select(query);
		} else {
			throw new UnsupportedOperationException("Cannot cope with prepared statements and values to set.");
		}
	}

	@Override
	public int[] insert(final String sql, final List<? extends Object> values) throws SQLException {
		return this.insert(this.queryBuilder.parseSQLCommand(sql, values));
	}

	@Override
	public int[] insertMultiple(final String table, final List<String> keys, final List<List<? extends Object>> datarows, final int chunkSize) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(final String sql, final List<? extends Object> values) throws SQLException {
		return this.update(this.queryBuilder.parseSQLCommand(sql, values));
	}

	@Override
	public void executeQueriesAtomically(final List<PreparedStatement> queries) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() {
		/* nothing to do */
	}

	@Override
	public int delete(final String table, final Map<String, ? extends Object> conditions) throws SQLException {
		StringBuilder conditionSB = new StringBuilder();
		for (Entry<String, ? extends Object> entry : conditions.entrySet()) {
			if (conditionSB.length() > 0) {
				conditionSB.append(STR_SPACE_AND);
			}
			if (entry.getValue() != null) {
				conditionSB.append(entry.getKey() + KEY_EQUALS_VALUE_TO_BE_SET);
			} else {
				conditionSB.append(entry.getKey());
				conditionSB.append(" IS NULL");
			}
		}
		return this.update("DELETE FROM `" + table + "`" + STR_SPACE_WHERE + " " + conditionSB);
	}

	@Override
	public boolean doesTableExist(final String tablename) throws SQLException, IOException {
		return this.getResultsOfQuery("SHOW TABLES").stream().anyMatch(r -> r.values().iterator().next().equals(tablename));
	}
}