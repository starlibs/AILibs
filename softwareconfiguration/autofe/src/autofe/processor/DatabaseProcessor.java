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
import autofe.db.util.DBUtils;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.probleminputs.GeneralEvaluatedTraversalTree;
import weka.core.Instances;

public class DatabaseProcessor {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseProcessor.class);

	private static final int TIMEOUT_F_COMPUTATION_MS = 10000;

	private Database database;

	private DatabaseAutoFeConfiguration configuration;

	private Instances instancesWithSelectedFeatures;

	private List<AbstractFeature> selectedFeatures;

	public DatabaseProcessor(DatabaseAutoFeConfiguration configuration, String databaseModelFile) {
		this.configuration = configuration;
		this.database = DBUtils.deserializeFromFile(databaseModelFile);
	}

	public DatabaseProcessor(DatabaseAutoFeConfiguration configuration, Database database) {
		this.configuration = configuration;
		this.database = database;
	}

	public void doFeatureSelection() {
		long timeout = System.currentTimeMillis() + configuration.getTimeoutInMs();

		// Setup
		DatabaseGraphGenerator generator = new DatabaseGraphGenerator(database);
		DatabaseNodeEvaluator evaluator = new DatabaseNodeEvaluator(generator,
				configuration.getRandomCompletionPathLength(), configuration.getSeed(),
				configuration.getEvaluationFunction());
		
		GeneralEvaluatedTraversalTree<DatabaseNode, String, Double> tree = new GeneralEvaluatedTraversalTree<>(generator, evaluator);
		BestFirst<GeneralEvaluatedTraversalTree<DatabaseNode, String, Double>,DatabaseNode, String, Double> search = new BestFirst<>(tree);
		search.setTimeoutForComputationOfF(TIMEOUT_F_COMPUTATION_MS, node -> 100.0);

		// Do search
		SearchGraphPath<DatabaseNode, String> solution = null;
		while (search.hasNext() && System.currentTimeMillis() < timeout) {
			try {
				solution = search.nextSolution();
			} catch (InterruptedException e) {
				LOG.warn("Search has been interrupted!");
			} catch (NoSuchElementException e) {
				LOG.error("An error occured in the search!",e);
			} catch (AlgorithmExecutionCanceledException e) {
				LOG.error("Search algorithm has been canceled!",e);
			}
		}

		if (solution == null) {
			throw new RuntimeException("No solution found!");
		}
		
		search.cancel();

		DatabaseNode goal = solution.getNodes().get(solution.getNodes().size() - 1);
		
		if(goal.getSelectedFeatures().isEmpty()) {
			throw new RuntimeException("Found a solution, but the feature list is empty!");
		}
		
		DatabaseConnector databaseConnector = evaluator.getDatabaseConnector();
		this.instancesWithSelectedFeatures = databaseConnector.getInstances(goal.getSelectedFeatures());
		this.selectedFeatures = goal.getSelectedFeatures();

		// Delete created tables
		databaseConnector.cleanup();

		databaseConnector.close();
	}

	public Instances getInstancesWithSelectedFeatures() {
		if (instancesWithSelectedFeatures == null) {
			throw new IllegalStateException("Instances have not been loaded yet!");
		}
		return instancesWithSelectedFeatures;
	}

	public List<AbstractFeature> getSelectedFeatures() {
		if (selectedFeatures == null) {
			throw new IllegalStateException("Features have not been selected yet!");
		}
		return selectedFeatures;
	}

}
