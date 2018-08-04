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

	private static Logger LOG = LoggerFactory.getLogger(DBUtils.class);

	private static final int ADDITIONAL_FEATURES = 2;

	private static final long SEED = 1;

	private DatabaseConnector databaseConnector;

	private DatabaseGraphGenerator generator;

	private Database db;

	private Random random;

	public DatabaseNodeEvaluator(DatabaseGraphGenerator generator) {
		this.generator = generator;
		this.db = generator.getDatabase();
		this.databaseConnector = new DatabaseConnectorImpl(db);
		this.random = new Random(SEED);
	}

	@Override
	public Double f(Node<DatabaseNode, ?> node) throws Throwable {
		LOG.info("Evaluation node with features : {}", node.getPoint().getSelectedFeatures());
		int requiredNumberOfFeatures = node.getPoint().getSelectedFeatures().size() + ADDITIONAL_FEATURES;
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
		DatabaseNode goalNode = solution.get(solution.size() - 1);
		LOG.info("Result of random completion is node with features : {}", goalNode.getSelectedFeatures());
		Instances instances = databaseConnector.getInstances(goalNode.getSelectedFeatures());
		LOG.debug(instances.toString());
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

}
