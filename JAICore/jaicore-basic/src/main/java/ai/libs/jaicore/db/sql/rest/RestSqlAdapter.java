package ai.libs.jaicore.db.sql.rest;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.api4.java.datastructure.kvstore.IKVStore;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class RestSqlAdapter implements IDatabaseAdapter {

	private final IRestDatabaseConfig config;

	public RestSqlAdapter(final IRestDatabaseConfig config) {
		this.config = config;
	}

	public List<IKVStore> select(final String query) throws SQLException {
		System.out.println(this.config.getHost() + this.config.getSelectSuffix());
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getSelectSuffix(), query);
		return KVStoreUtil.readFromJson(res);
	}

	@Override
	public int[] insert(final String table, final Map<String, ? extends Object> values) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder();
		List<String> keys = new LinkedList<>(values.keySet());
		queryBuilder.append("INSERT INTO " + table + "(");
		queryBuilder.append(keys.stream().collect(Collectors.joining(",")));
		queryBuilder.append(") VALUES ('");
		queryBuilder.append(keys.stream().map(x -> values.get(x) + "").collect(Collectors.joining("','")));
		queryBuilder.append("')");
		return this.insert(queryBuilder.toString());
	}

	@Override
	public int[] insertMultiple(final String tablename, final List<String> keys, final List<List<?>> values) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("INSERT INTO " + tablename + " (");
		queryBuilder.append(keys.stream().collect(Collectors.joining(",")));
		queryBuilder.append(") VALUES (");
		queryBuilder.append(values.stream().map(x -> "'" + x.stream().map(y -> y + "").collect(Collectors.joining("','")) + "'").collect(Collectors.joining("), (")));
		queryBuilder.append(")");
		return this.insert(queryBuilder.toString());
	}

	public int[] insert(final String query) throws SQLException {
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getInsertSuffix(), query);
		if (res instanceof ArrayNode) {
			ArrayNode array = (ArrayNode) res;
			return IntStream.range(0, array.size()).map(i -> array.get(i).asInt()).toArray();
		} else {
			throw new IllegalStateException("Cannot parse result for insert query");
		}
	}

	@Override
	public int update(final String query) throws SQLException {
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getUpdateSuffix(), query);
		return res.asInt();
	}

	@Override
	public List<IKVStore> query(final String query) throws SQLException {
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getQuerySuffix(), query);
		try {
			System.out.println(new ObjectMapper().writeValueAsString(res));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return KVStoreUtil.readFromJson(res);
	}

	public JsonNode executeRESTCall(final String URL, final String query) throws SQLException {
		try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode root = mapper.createObjectNode();
			root.set("token", root.textNode(this.config.getToken()));
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
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("UPDATE " + tablename + " SET ");
		queryBuilder.append(valuesToWrite.entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(Collectors.joining(",")));
		if (!where.isEmpty()) {
			queryBuilder.append(" WHERE ");
			queryBuilder.append(where.entrySet().stream().map(e -> RestSqlAdapter.whereClauseElement(e.getKey(), e.getValue().toString())).collect(Collectors.joining(" AND ")));
		}
		return this.update(queryBuilder.toString());
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLoggerName(final String name) {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public int[] insertMultiple(final String table, final List<String> keys, final List<List<? extends Object>> datarows, final int chunkSize) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(final String sql, final List<? extends Object> values) throws SQLException {
		throw new UnsupportedOperationException();
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