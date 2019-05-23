package autofe.db.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import autofe.db.configuration.DatabaseAutoFeConfiguration;
import autofe.db.model.database.AbstractFeature;
import autofe.processor.DatabaseProcessor;

public class FullTest {

	public static final int TIMEOUT_IN_MS = 2000000;

	public static final long SEED = 1;

	public static final double FE_FRACTION = 0.5;

	public static final int RC_PATHLENGTH = 2;

	public static final String EVALUATION_FUNCTION = "Cluster";

	public static final String DATABASE_MODEL_FILE = "model/db/bankaccount_toy_database.json";

	@Test
	public void doFullTest() throws InterruptedException {
		// Phase 1: Select features
		DatabaseAutoFeConfiguration config = new DatabaseAutoFeConfiguration(RC_PATHLENGTH, EVALUATION_FUNCTION, SEED, (int) (TIMEOUT_IN_MS * FE_FRACTION));
		DatabaseProcessor dbProcessor = new DatabaseProcessor(config, DATABASE_MODEL_FILE);
		dbProcessor.doFeatureSelection();

		List<AbstractFeature> selectedFeatures = dbProcessor.getSelectedFeatures();

		System.out.println("Features are : " + selectedFeatures);

		assertTrue(!selectedFeatures.isEmpty());

	}

}
