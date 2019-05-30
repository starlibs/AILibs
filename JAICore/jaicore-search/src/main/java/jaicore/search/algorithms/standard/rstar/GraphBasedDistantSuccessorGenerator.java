package jaicore.search.algorithms.standard.rstar;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.IMetric;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.model.travesaltree.NodeExpansionDescription;
import jaicore.search.probleminputs.GraphSearchWithNumberBasedAdditivePathEvaluationAndSubPathHeuristic.DistantSuccessorGenerator;
import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public class GraphBasedDistantSuccessorGenerator<N, A> implements DistantSuccessorGenerator<N>, ILoggingCustomizable {

	private static final int MAX_ATTEMPTS = 10;
	private final SuccessorGenerator<N, A> succesorGenerator;
	private final NodeGoalTester<N> goalTester;
	private final Random random;

	private Logger logger = LoggerFactory.getLogger(GraphBasedDistantSuccessorGenerator.class);

	public GraphBasedDistantSuccessorGenerator(final GraphGenerator<N, A> graphGenerator, final int seed) {
		super();
		this.succesorGenerator = graphGenerator.getSuccessorGenerator();
		this.goalTester = (NodeGoalTester<N>)graphGenerator.getGoalTester();
		this.random = new Random(seed);
	}

	@Override
	public List<N> getDistantSuccessors(final N n, final int k, final IMetric<N> metricOverStates, final double delta) throws InterruptedException {
		List<N> successorsInOriginalGraph = new ArrayList<>();
		if (this.goalTester.isGoal(n)) {
			return successorsInOriginalGraph;
		}
		for (int i = 0; i < MAX_ATTEMPTS && successorsInOriginalGraph.size() < k; i++) {
			this.logger.debug("Drawing next distant successor for {}. {}/{} have already been drawn. This is the {}-th attempt.", n, successorsInOriginalGraph.size(), k, i + 1);
			N candidatePoint = n;

			/* detect potential dead end */
			boolean deadEnd = false;
			while (!this.goalTester.isGoal(candidatePoint) && metricOverStates.getDistance(n, candidatePoint) <= delta) {
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Successor generation has been interrupted.");
				}
				assert !this.goalTester.isGoal(candidatePoint) : "Node must not be a goal node!";
				List<NodeExpansionDescription<N, A>> localSuccessors = this.succesorGenerator.generateSuccessors(candidatePoint);
				if (localSuccessors.isEmpty()) {
					this.logger.warn("List of local successors is empty for node {}! This may be due to a dead-end in the search graph.", candidatePoint);
					deadEnd = true;
					break;
				}
				candidatePoint = localSuccessors.size() > 1 ? localSuccessors.get(this.random.nextInt(localSuccessors.size() - 1)).getTo() : localSuccessors.get(0).getTo();
				this.logger.trace("Next node on path to distant successor is {}", candidatePoint);
			}
			if (deadEnd) {
				this.logger.debug("Skipping this candidate, because it is a dead-end.");
				continue;
			}

			/* check that we really have a node different from the one we expand here */
			if (candidatePoint == n) {
				if (this.goalTester.isGoal(candidatePoint)) {
					throw new IllegalStateException("The last point is the point we want to extend. The reason is that this point is already a goal node.");
				}
				else if (metricOverStates.getDistance(n, candidatePoint) > delta) {
					throw new IllegalStateException("The last point is the point we want to extend. The reason is that the chosen node had a two high delta " + metricOverStates.getDistance(n, candidatePoint) + ".");
				}
				else {
					throw new IllegalStateException("The last point is the point we want to extend. The reason is unclear at this point.");
				}
			}

			/* add the node if we don't have it yet */
			if (!successorsInOriginalGraph.contains(candidatePoint)) {
				successorsInOriginalGraph.add(candidatePoint);
			}
		}
		this.logger.info("Returning {} successors.", successorsInOriginalGraph.size());
		return successorsInOriginalGraph;
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}
}
