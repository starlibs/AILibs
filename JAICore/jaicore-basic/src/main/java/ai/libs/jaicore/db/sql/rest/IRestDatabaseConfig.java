package ai.libs.jaicore.db.sql.rest;

import org.aeonbits.owner.Reloadable;

import ai.libs.jaicore.basic.IOwnerBasedConfig;

public interface IRestDatabaseConfig extends IOwnerBasedConfig, Reloadable {

	public static final String K_REST_DB_HOST = "sql.rest.host";
	public static final String K_REST_DB_URL_INSERT = "sql.rest.host.insert";
	public static final String K_REST_DB_URL_UPDATE = "sql.rest.host.update";
	public static final String K_REST_DB_URL_SELECT = "sql.rest.host.select";
	public static final String K_REST_DB_URL_QUERY = "sql.rest.host.query";
	public static final String K_REST_DB_TOKEN = "sql.rest.token";

	public static final String K_REST_DB_TABLE = "sql.rest.table";

	@Key(K_REST_DB_HOST)
	public String getHost();

	@Key(K_REST_DB_TOKEN)
	public String getToken();

	@Key(K_REST_DB_URL_INSERT)
	@DefaultValue("/insert")
	public String getInsertSuffix();

	@Key(K_REST_DB_URL_UPDATE)
	@DefaultValue("/update")
	public String getUpdateSuffix();

	@Key(K_REST_DB_URL_SELECT)
	@DefaultValue("/query")
	public String getSelectSuffix();

	@Key(K_REST_DB_URL_QUERY)
	@DefaultValue("/query")
	public String getQuerySuffix();

	@Key(K_REST_DB_TABLE)
	public String getTable();

}
