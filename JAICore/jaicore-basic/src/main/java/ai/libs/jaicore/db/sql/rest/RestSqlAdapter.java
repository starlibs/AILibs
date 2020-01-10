package ai.libs.jaicore.db.sql.rest;

import java.io.IOException;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ai.libs.jaicore.basic.kvstore.IKVStore;
import ai.libs.jaicore.basic.kvstore.KVStoreUtil;

/**
 * This is a simple util class for easy database access and query execution in sql. You need to make sure that the respective JDBC connector is in the class path. By default, the adapter uses the mysql driver, but any jdbc driver can be
 * used.
 *
 * @author fmohr, mwever
 *
 */
@SuppressWarnings("serial")
public class RestSqlAdapter {

	private final IRestDatabaseConfig config;

	public RestSqlAdapter(final IRestDatabaseConfig config) {
		this.config = config;
	}

	public List<IKVStore> select(final String query) throws IOException {
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getSelectSuffix(), query);
		return KVStoreUtil.readFromJson(res);
	}

	public int[] insert(final String table, final Map<String, Object> values) throws IOException {
		StringBuilder queryBuilder = new StringBuilder();
		List<String> keys = new LinkedList<>(values.keySet());
		queryBuilder.append("INSERT INTO " + table + "(");
		queryBuilder.append(keys.stream().collect(Collectors.joining(",")));
		queryBuilder.append(") VALUES ('");
		queryBuilder.append(keys.stream().map(x -> values.get(x) + "").collect(Collectors.joining("','")));
		queryBuilder.append("')");
		return this.insert(queryBuilder.toString());
	}

	public int[] insertMultiple(final String tablename, final List<String> keys, final List<List<?>> values) throws IOException {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("INSERT INTO " + tablename + " (");
		queryBuilder.append(keys.stream().collect(Collectors.joining(",")));
		queryBuilder.append(") VALUES (");
		queryBuilder.append(values.stream().map(x -> "'" + x.stream().map(y -> y + "").collect(Collectors.joining("','")) + "'").collect(Collectors.joining("), (")));
		queryBuilder.append(")");
		return this.insert(queryBuilder.toString());
	}

	public int[] insert(final String query) throws IOException {
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getInsertSuffix(), query);
		if (res instanceof ArrayNode) {
			ArrayNode array = (ArrayNode) res;
			return IntStream.range(0, array.size()).map(i -> array.get(i).asInt()).toArray();
		} else {
			throw new IllegalStateException("Cannot parse result for insert query");
		}
	}

	public int update(final String query) throws IOException {
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getUpdateSuffix(), query);
		return res.asInt();
	}

	public List<IKVStore> query(final String query) throws IOException {
		JsonNode res = this.executeRESTCall(this.config.getHost() + this.config.getQuerySuffix(), query);
		return KVStoreUtil.readFromJson(res);
	}

	public JsonNode executeRESTCall(final String URL, final String query) throws IOException {
		CloseableHttpClient client = HttpClientBuilder.create().build();
		try {
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
		} finally {
			client.close();
		}
	}

	public int update(final String tablename, final Map<String, String> valuesToWrite, final Map<String, String> where) throws IOException {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("UPDATE " + tablename + " SET ");
		queryBuilder.append(valuesToWrite.entrySet().stream().map(e -> e.getKey() + "='" + e.getValue() + "'").collect(Collectors.joining(",")));
		if (!where.isEmpty()) {
			queryBuilder.append(" WHERE ");
			queryBuilder.append(where.entrySet().stream().map(e -> RestSqlAdapter.whereClauseElement(e.getKey(), e.getValue())).collect(Collectors.joining(" AND ")));
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

}