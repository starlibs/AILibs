package autofe.db.test;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

import autofe.db.configuration.DatabaseAutoFeConfiguration;
import autofe.db.model.database.AbstractFeature;
import autofe.db.sql.RetrieveInstancesFromDatabaseFailedException;
import autofe.processor.DatabaseProcessor;

public class FullTest {

	public static final int TIMEOUT_IN_MS = 2000000;

	public static final long SEED = 1;

	public static final double FE_FRACTION = 0.5;

	public static final int RC_PATHLENGTH = 2;

	public static final String EVALUATION_FUNCTION = "Cluster";

	public static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	@Test
	public void doFullTest() throws InterruptedException, RetrieveInstancesFromDatabaseFailedException {
		// Phase 1: Select features
		Map<String, Object> props = new HashMap<>();
		props.put(DatabaseAutoFeConfiguration.K_EVALUATION_FUNCTION, EVALUATION_FUNCTION);
		props.put(DatabaseAutoFeConfiguration.K_SEED, SEED);
		props.put(DatabaseAutoFeConfiguration.K_RANDOM_COMPLETION_PATH_LENGTH, RC_PATHLENGTH);
		props.put(DatabaseAutoFeConfiguration.K_TIMEOUT, TIMEOUT_IN_MS);
		DatabaseAutoFeConfiguration config = ConfigFactory.create(DatabaseAutoFeConfiguration.class, props);

		DatabaseProcessor dbProcessor = new DatabaseProcessor(config, DATABASE_MODEL_FILE);
		dbProcessor.doFeatureSelection();

		List<AbstractFeature> selectedFeatures = dbProcessor.getSelectedFeatures();

		System.out.println("Features are : " + selectedFeatures);

		assertTrue(!selectedFeatures.isEmpty());

	}

}
