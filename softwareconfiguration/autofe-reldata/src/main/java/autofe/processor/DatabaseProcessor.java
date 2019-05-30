package autofe.processor;

import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.configuration.DatabaseAutoFeConfiguration;
import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.Database;
import autofe.db.search.DatabaseGraphGenerator;
import autofe.db.search.DatabaseNode;
import autofe.db.search.DatabaseNodeEvaluator;
import autofe.db.sql.DatabaseConnector;
import autofe.db.sql.RetrieveInstancesFromDatabaseFailedException;
import autofe.db.util.DBUtils;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.probleminputs.GraphSearchWithSubpathEvaluationsInput;
import weka.core.Instances;

public class DatabaseProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseProcessor.class);

	private static final int TIMEOUT_F_COMPUTATION_MS = 10000;

	private Database database;

	private DatabaseAutoFeConfiguration configuration;

	private Instances instancesWithSelectedFeatures;

	private List<AbstractFeature> selectedFeatures;

	public DatabaseProcessor(final DatabaseAutoFeConfiguration configuration, final String databaseModelFile) {
		this.configuration = configuration;
		this.database = DBUtils.deserializeFromFile(databaseModelFile);
	}

	public DatabaseProcessor(final DatabaseAutoFeConfiguration configuration, final Database database) {
		this.configuration = configuration;
		this.database = database;
	}

	public void doFeatureSelection() throws InterruptedException, RetrieveInstancesFromDatabaseFailedException {
		long timeout = System.currentTimeMillis() + this.configuration.getTimeoutInMs();

		// Setup
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(this.database);
		DatabaseNodeEvaluator evaluator = new DatabaseNodeEvaluator(generator, this.configuration.getRandomCompletionPathLength(), this.configuration.getSeed(), this.configuration.getEvaluationFunction());

		GraphSearchWithSubpathEvaluationsInput<DatabaseNode, String, Double> tree = new GraphSearchWithSubpathEvaluationsInput<>(generator, evaluator);
		BestFirst<GraphSearchWithSubpathEvaluationsInput<DatabaseNode, String, Double>, DatabaseNode, String, Double> search = new BestFirst<>(tree);
		search.setTimeoutForComputationOfF(TIMEOUT_F_COMPUTATION_MS, node -> 100.0);

		// Do search
		SearchGraphPath<DatabaseNode, String> solution = null;
		while (search.hasNext() && System.currentTimeMillis() < timeout) {
			try {
				solution = search.nextSolutionCandidate();
			} catch (NoSuchElementException e) {
				LOGGER.error("An error occured in the search!", e);
			} catch (AlgorithmExecutionCanceledException e) {
				LOGGER.error("Search algorithm has been canceled!", e);
			} catch (AlgorithmTimeoutedException e) {
				LOGGER.error("Search algorithm has timeouted!", e);
			} catch (AlgorithmException e) {
				LOGGER.error("An exception occurred while searching!", e);
			}
		}

		if (solution == null) {
			throw new NoSolutionFoundException("No solution found!");
		}

		search.cancel();

		DatabaseNode goal = solution.getNodes().get(solution.getNodes().size() - 1);

		if (goal.getSelectedFeatures().isEmpty()) {
			throw new NoSolutionFoundException("Found a solution, but the feature list is empty!");
		}

		DatabaseConnector databaseConnector = evaluator.getDatabaseConnector();
		this.instancesWithSelectedFeatures = databaseConnector.getInstances(goal.getSelectedFeatures());
		this.selectedFeatures = goal.getSelectedFeatures();

		// Delete created tables
		databaseConnector.cleanup();

		databaseConnector.close();
	}

	public Instances getInstancesWithSelectedFeatures() {
		if (this.instancesWithSelectedFeatures == null) {
			throw new IllegalStateException("Instances have not been loaded yet!");
		}
		return this.instancesWithSelectedFeatures;
	}

	public List<AbstractFeature> getSelectedFeatures() {
		if (this.selectedFeatures == null) {
			throw new IllegalStateException("Features have not been selected yet!");
		}
		return this.selectedFeatures;
	}

}
