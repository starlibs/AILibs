package autofe.db.search;

import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import autofe.db.model.database.AbstractFeature;
import autofe.db.model.database.BackwardFeature;
import autofe.db.model.database.Database;
import autofe.db.sql.DatabaseConnector;
import autofe.db.sql.DatabaseConnectorImpl;
import autofe.db.util.DBUtils;
import autofe.util.EvaluationUtils;
import jaicore.graphvisualizer.SimpleGraphVisualizationWindow;
import jaicore.search.algorithms.standard.bestfirst.BestFirst;
import jaicore.search.algorithms.standard.core.INodeEvaluator;
import jaicore.search.algorithms.standard.rdfs.RandomizedDepthFirstSearch;
import jaicore.search.structure.core.GraphGenerator;
import jaicore.search.structure.core.Node;
import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SingleRootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;
import weka.core.Instances;

public class DatabaseNodeEvaluator implements INodeEvaluator<DatabaseNode, Double> {

	private static Logger LOG = LoggerFactory.getLogger(DatabaseNodeEvaluator.class);

	private static final int RANDOM_COMPLETION_PATH_LENGTH = 2;

	private static final long SEED = 1;

	private int randomCompletionPathLength;

	private long seed;

	private DatabaseConnector databaseConnector;

	private DatabaseGraphGenerator generator;

	private Database db;

	private Random random;

	public DatabaseNodeEvaluator(DatabaseGraphGenerator generator) {
		// Only use this constructor for test purposes
		this.generator = generator;
		this.randomCompletionPathLength = RANDOM_COMPLETION_PATH_LENGTH;
		this.seed = SEED;
		this.db = generator.getDatabase();
		this.databaseConnector = new DatabaseConnectorImpl(db);
		this.random = new Random(seed);
	}

	public DatabaseNodeEvaluator(DatabaseGraphGenerator generator, int randomCompletionPathLength, long seed) {
		this.generator = generator;
		this.randomCompletionPathLength = randomCompletionPathLength;
		this.seed = seed;
		this.db = generator.getDatabase();
		this.databaseConnector = new DatabaseConnectorImpl(db);
		this.random = new Random(seed);
	}

	@Override
	public Double f(Node<DatabaseNode, ?> node) throws Throwable {
		if (node.getPoint().getSelectedFeatures().isEmpty()) {
			LOG.warn("Return default value (0) for empty node");
			return new Double(0);
		}
		if (node.getPoint().isFinished()) {
			LOG.warn("Skip random completion for finished node!");
			Instances instances = databaseConnector.getInstances(node.getPoint().getSelectedFeatures());
			double result = evaluateInstances(instances);
			LOG.debug("Evaluation result (without random completion) is {}", result);
			return result;
		}
		LOG.info("Evaluation node with features : {}", node.getPoint().getSelectedFeatures());
		int requiredNumberOfFeatures = node.getPoint().getSelectedFeatures().size() + randomCompletionPathLength;
		LOG.debug("Required features : {}", requiredNumberOfFeatures);
		BestFirst<DatabaseNode, String> randomCompletionSearch = new RandomizedDepthFirstSearch<>(
				new GraphGenerator<DatabaseNode, String>() {

					@Override
					public RootGenerator<DatabaseNode> getRootGenerator() {
						return new SingleRootGenerator<DatabaseNode>() {
							@Override
							public DatabaseNode getRoot() {
								return node.getPoint();
							}
						};
					}

					@Override
					public SuccessorGenerator<DatabaseNode, String> getSuccessorGenerator() {
						return generator.getSuccessorGenerator();
					}

					@Override
					public GoalTester<DatabaseNode> getGoalTester() {
						return new NodeGoalTester<DatabaseNode>() {

							@Override
							public boolean isGoal(DatabaseNode node) {
								if (node.getSelectedFeatures().size() > requiredNumberOfFeatures) {
									throw new IllegalStateException(
											String.format("Too many features! Required: %s , Actual: %s",
													requiredNumberOfFeatures, node.getSelectedFeatures().size()));
								} else if (node.getSelectedFeatures().size() < requiredNumberOfFeatures) {
									return false;
								} else {
									// Check whether node contains intermediate features
									for (AbstractFeature feature : node.getSelectedFeatures()) {
										if (feature instanceof BackwardFeature
												&& DBUtils.isIntermediate(((BackwardFeature) feature).getPath(), db)) {
											return false;
										}
									}
									return true;
								}
							}
						};
					}

					@Override
					public boolean isSelfContained() {
						// TODO Auto-generated method stub
						return false;
					}

					@Override
					public void setNodeNumbering(boolean nodenumbering) {
						// TODO Auto-generated method stub

					}
				}, this.random);

		List<DatabaseNode> solution = randomCompletionSearch.nextSolution();
		if (solution == null) {
			throw new RuntimeException("Random completion did not find a solution!");
		}
		DatabaseNode goalNode = solution.get(solution.size() - 1);
		LOG.info("Result of random completion is node with features : {}", goalNode.getSelectedFeatures());
		Instances instances = databaseConnector.getInstances(goalNode.getSelectedFeatures());
		double result = evaluateInstances(instances);
		LOG.debug("Evaluation result is {}", result);
		return result;
	}

	private double evaluateInstances(Instances instances) {
		try {
			return EvaluationUtils.calculateCOCOForBatch(instances);
		} catch (Exception e) {
			throw new RuntimeException("Cannot evaluate instances", e);
		}
	}

	public DatabaseConnector getDatabaseConnector() {
		return databaseConnector;
	}

}
