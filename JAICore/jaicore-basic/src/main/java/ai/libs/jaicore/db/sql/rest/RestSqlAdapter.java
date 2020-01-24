package ai.libs.jaicore.db.sql.rest;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.api4.java.datastructure.kvstore.IKVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.kvstore.KVStoreUtil;
import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.sql.ISQLQueryBuilder;
import ai.libs.jaicore.db.sql.MySQLQueryBuilder;

/**
 * This is a simple util class for easy database access and query execution in sql. You need to make sure that the respective JDBC connector is in the class path. By default, the adapter uses the mysql driver, but any jdbc driver can be
 * used.
 *
 * @author fmohr, mwever
 *
 */
@SuppressWarnings("serial")
public class RestSqlAdapter implements IDatabaseAdapter {

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

	public int[] insert(final String query) throws SQLException {
		JsonNode res = this.executeRESTCall(this.host + this.insertSuffix, query);
		if (res instanceof ArrayNode) {
			ArrayNode array = (ArrayNode) res;
			return IntStream.range(0, array.size()).map(i -> array.get(i).asInt()).toArray();
		} else {
			throw new IllegalStateException("Cannot parse result for insert query");
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
			String jsonPayload = mapper.writeValueAsString(root);
			StringEntity requestEntity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
			HttpPost post = new HttpPost(URL);
			post.setEntity(requestEntity);
			CloseableHttpResponse response = client.execute(post);
			return mapper.readTree(response.getEntity().getContent());
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
			queryStringBuilder.append(" WHERE ");
			queryStringBuilder.append(where.entrySet().stream().map(e -> RestSqlAdapter.whereClauseElement(e.getKey(), e.getValue() != null ? e.getValue().toString() : null)).collect(Collectors.joining(" AND ")));
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
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

}