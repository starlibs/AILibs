package ai.libs.jaicore.db;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Rule;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.rules.Timeout;

import ai.libs.jaicore.basic.ATest;
import ai.libs.jaicore.db.sql.DatabaseAdapterFactory;
import ai.libs.jaicore.db.sql.IRestDatabaseConfig;

public abstract class DBTest extends ATest {

	public static final String VAR_DB_HOST = "AILIBS_JAICORE_DB_DEFAULT_HOST";
	public static final String VAR_DB_USER = "AILIBS_JAICORE_DB_DEFAULT_USER";
	public static final String VAR_DB_PASS = "AILIBS_JAICORE_DB_DEFAULT_PASS";
	public static final String VAR_DB_DATABASE = "AILIBS_JAICORE_DB_DEFAULT_DATABASE";


	public static final String VAR_DB_REST_HOST = "AILIBS_JAICORE_DB_REST_DB_HOST";
	public static final String VAR_DB_REST_TOKEN = "AILIBS_JAICORE_DB_REST_DB_TOKEN"; // this is for rest-based access

	@Rule
	public Timeout globalTimeout = Timeout.seconds(30); // database tests should not take longer than 10 seconds

	protected IDatabaseAdapter reportConfigAndGetAdapter(final Object config) {
		if (config instanceof IDatabaseConfig) {
			IDatabaseConfig c = (IDatabaseConfig )config;
			this.logger.info("Carry out tests with direct MySQL connection to {} on database {} with user {}.", c.getDBHost(), c.getDBDatabaseName(), c.getDBUsername());
			return DatabaseAdapterFactory.get(c);
		}
		else if (config instanceof IRestDatabaseConfig) {
			IRestDatabaseConfig c = (IRestDatabaseConfig)config;
			this.logger.info("Carry out tests with REST connection to {} using token {}.", c.getHost(), c.getToken());
			return DatabaseAdapterFactory.get(c);
		}
		else {
			throw new IllegalArgumentException("Cannot work with configs of type " + config.getClass());
		}
	}

	public static Stream<Arguments> getDatabaseConfigs() throws IOException {

		/* configure standard DB adapter */
		IDatabaseConfig configDefault = ConfigFactory.create(IDatabaseConfig.class);
		configDefault.setProperty(IDatabaseConfig.DB_DRIVER, "mysql");
		configDefault.setProperty(IDatabaseConfig.DB_HOST, System.getenv(VAR_DB_HOST));
		configDefault.setProperty(IDatabaseConfig.DB_USER, System.getenv(VAR_DB_USER));
		configDefault.setProperty(IDatabaseConfig.DB_PASS, System.getenv(VAR_DB_PASS));
		configDefault.setProperty(IDatabaseConfig.DB_NAME, System.getenv(VAR_DB_DATABASE));
		configDefault.setProperty(IDatabaseConfig.DB_SSL, "true");
		Objects.requireNonNull(configDefault.getDBHost(), "The host information read from environment variable " + VAR_DB_HOST + " is NULL!");
		Objects.requireNonNull(configDefault.getDBUsername(), "The user information read from environment variable " + VAR_DB_USER + " is NULL!");
		Objects.requireNonNull(configDefault.getDBPassword(), "The password read from environment variable " + VAR_DB_PASS + " is NULL!");
		Objects.requireNonNull(configDefault.getDBDatabaseName(), "The database name read from environment variable " + VAR_DB_DATABASE + " is NULL!");

		/* configure REST DB adapter */
		IRestDatabaseConfig configRest = ConfigFactory.create(IRestDatabaseConfig.class);
		configRest.setProperty(IRestDatabaseConfig.K_REST_DB_HOST, System.getenv(VAR_DB_REST_HOST));
		configRest.setProperty(IRestDatabaseConfig.K_REST_DB_TOKEN, System.getenv(VAR_DB_REST_TOKEN));
		if (configRest.getHost() == null || configRest.getToken() == null) {
			LOGGER.error("The host and the token for the REST DB connection could not be loaded. Either add the proper values to the properties file or via environment variables '{}' and '{}'", VAR_DB_REST_HOST, VAR_DB_REST_TOKEN);
			throw new IllegalArgumentException("Could not load host and token information information");
		}
		assertNotNull(configRest.getHost());
		return Stream.of(Arguments.of(configDefault), Arguments.of(configRest));
	}
}
