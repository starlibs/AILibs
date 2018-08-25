package autofe.processor;

import java.util.List;

import autofe.db.configuration.DatabaseAutoFeConfiguration;
import autofe.db.model.database.Database;
import autofe.db.search.DatabaseGraphGenerator;
import autofe.db.search.DatabaseNode;
import autofe.db.search.DatabaseNodeEvaluator;
import autofe.db.sql.DatabaseConnector;
import autofe.db.util.DBUtils;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import weka.core.Instances;

public class DatabaseProcessor {

	private static final int TIMEOUT_F_COMPUTATION_MS = 10000;

	private Database database;

	private DatabaseAutoFeConfiguration configuration;

	public DatabaseProcessor(DatabaseAutoFeConfiguration configuration, String databaseModelFile) {
		this.configuration = configuration;
		this.database = DBUtils.deserializeFromFile(databaseModelFile);
	}

	public DatabaseProcessor(DatabaseAutoFeConfiguration configuration, Database database) {
		this.configuration = configuration;
		this.database = database;
	}

	public Instances selectFeatures() {
		long timeout = System.currentTimeMillis() + configuration.getTimeoutInMs();

		// Setup
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(database);
		DatabaseNodeEvaluator evaluator = new DatabaseNodeEvaluator(generator,
				configuration.getRandomCompletionPathLength(), configuration.getSeed(),
				configuration.getEvaluationFunction());
		BestFirst<DatabaseNode, String> search = new BestFirst<>(generator, evaluator);
		search.setTimeoutForComputationOfF(TIMEOUT_F_COMPUTATION_MS, node -> 100.0);

		// Do search
		List<DatabaseNode> solution = null;
		while (search.hasNext() && System.currentTimeMillis() < timeout) {
			solution = search.nextSolution();
		}

		DatabaseNode goal = solution.get(solution.size() - 1);
		DatabaseConnector databaseConnector = evaluator.getDatabaseConnector();
		Instances instances = databaseConnector.getInstances(goal.getSelectedFeatures());

		// Delete created tables
		databaseConnector.cleanup();

		databaseConnector.close();

		return instances;
	}

}
