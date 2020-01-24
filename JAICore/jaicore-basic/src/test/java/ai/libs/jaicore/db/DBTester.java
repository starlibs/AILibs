package ai.libs.jaicore.db;

import java.io.IOException;

import ai.libs.jaicore.basic.Tester;
import ai.libs.jaicore.db.sql.rest.IRestDatabaseConfig;

public abstract class DBTester extends Tester {

	public static final String VAR_DB_HOST = "AILIBS_JAICORE_DB_REST_DB_HOST";
	public static final String VAR_DB_TOKEN = "AILIBS_JAICORE_DB_REST_DB_TOKEN";

	public static IRestDatabaseConfig setConnectionConfigIfEmpty(final IRestDatabaseConfig config) throws IOException {
		if (config.getHost() == null || config.getHost().trim().isEmpty()) {
			String val = System.getenv(VAR_DB_HOST);
			LOGGER.info("Reading host from environment variable {}. Value: {}", VAR_DB_HOST, val);
			config.setProperty(IRestDatabaseConfig.K_REST_DB_HOST, val);
		}
		if (config.getToken() == null || config.getToken().trim().isEmpty()) {
			String val = System.getenv(VAR_DB_TOKEN);
			LOGGER.info("Reading token from environment variable {}. Value: {}", VAR_DB_TOKEN, val);
			config.setProperty(IRestDatabaseConfig.K_REST_DB_TOKEN, val);
		}

		if (config.getHost() == null || config.getToken() == null) {
			LOGGER.error("The host and the token for the REST DB connection could not be loaded. Either add the proper values to the properties file or via environment variables '{}' and '{}'", VAR_DB_HOST, VAR_DB_TOKEN);
			throw new IllegalArgumentException("Could not load host and token information information");
		} else {
			LOGGER.info("Carry out tests with server backend at {} with token {}.", config.getHost(), config.getToken());
		}
		return config;
	}

	public IDatabaseConfig setConnectionConfigIfEmpty(final IDatabaseConfig config) {
		if (config.getDBHost() == null || config.getDBHost().trim().isEmpty()) {
			String val = System.getenv(VAR_DB_HOST);
			LOGGER.info("Reading host from environment variable {}. Value: {}", VAR_DB_HOST, val);
			config.setProperty(IDatabaseConfig.DB_HOST, val);
		}

		if (config.getDBHost() == null) {
			LOGGER.error("The host and the token for the REST DB connection could not be loaded. Either add the proper values to the properties file or via environment variables '{}' and '{}'", VAR_DB_HOST, VAR_DB_TOKEN);
			throw new IllegalArgumentException("Could not load host and token information information");
		} else {
			LOGGER.info("Carry out tests with server backend at {}.", config.getDBHost());
		}
		return config;
	}
}
