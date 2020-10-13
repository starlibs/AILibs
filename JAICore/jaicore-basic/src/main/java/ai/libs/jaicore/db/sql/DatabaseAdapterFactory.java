package ai.libs.jaicore.db.sql;

import java.util.Objects;

import ai.libs.jaicore.db.IDatabaseAdapter;
import ai.libs.jaicore.db.IDatabaseConfig;

public class DatabaseAdapterFactory {

	private DatabaseAdapterFactory() {
		/* avoid instantiation */
	}

	public static IDatabaseAdapter get(final IDatabaseConfig config) {
		Objects.requireNonNull(config);
		return new SQLAdapter(config);
	}

	public static IDatabaseAdapter get(final IRestDatabaseConfig config) {
		Objects.requireNonNull(config);
		return new RestSqlAdapter(config);
	}
}
