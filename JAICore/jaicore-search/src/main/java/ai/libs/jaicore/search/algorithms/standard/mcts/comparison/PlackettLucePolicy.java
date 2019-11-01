package ai.libs.jaicore.search.algorithms.standard.mcts.comparison;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.api4.java.datastructure.graph.IPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

import ai.libs.jaicore.basic.events.IEventEmitter;
import ai.libs.jaicore.graph.LabeledGraph;
import ai.libs.jaicore.math.probability.pl.PLInferenceProblem;
import ai.libs.jaicore.math.probability.pl.PLInferenceProblemEncoder;
import ai.libs.jaicore.math.probability.pl.PLMMAlgorithm;
import ai.libs.jaicore.search.algorithms.standard.mcts.ActionPredictionFailedException;
import ai.libs.jaicore.search.algorithms.standard.mcts.IGraphDependentPolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.IPathUpdatablePolicy;
import ai.libs.jaicore.search.algorithms.standard.mcts.UniformRandomPolicy;
import it.unimi.dsi.fastutil.doubles.DoubleList;

public class PlackettLucePolicy<N, A> implements IPathUpdatablePolicy<N, A, Double>, ILoggingCustomizable, IGraphDependentPolicy<N, A>, IEventEmitter {

	private final EventBus eventBus = new EventBus();
	private Logger logger = LoggerFactory.getLogger(BradleyTerryLikelihoodPolicy.class);
	private final IPreferenceKernel<N, A> preferenceKernel;
	private final Map<N, DoubleList> skillVectorsForNodes = new HashMap<>();
	private final Random random;
	private final UniformRandomPolicy<N, A, Double> randomPolicy;

	public PlackettLucePolicy(final IPreferenceKernel<N, A> preferenceKernel, final Random random) {
		super();
		this.preferenceKernel = preferenceKernel;
		this.random = random;
		this.randomPolicy = new UniformRandomPolicy<>(new Random(random.nextLong()));
	}

	@Override
	public A getAction(final N node, final Map<A, N> actionsWithSuccessors) throws ActionPredictionFailedException {

		if (!this.preferenceKernel.canProduceReliableRankings(node)) {
			return this.randomPolicy.getAction(node, actionsWithSuccessors);
		}

		/* get likelihood for children */
		try {
			//			System.out.println("Computing PL-Problem instance");
			PLInferenceProblemEncoder encoder = new PLInferenceProblemEncoder();
			PLInferenceProblem problem = encoder.encode(this.preferenceKernel.getRankingsForChildrenOfNode(node));
			//			System.out.println("Start computation of skills for " + node);
			long start = System.currentTimeMillis();
			DoubleList skills = new PLMMAlgorithm(problem, this.skillVectorsForNodes.get(node)).call();
			this.skillVectorsForNodes.put(node, skills);
			//			System.out.println("Found best child " + bestChild + " with skill " + skills.get(bestChild) + " after " + (System.currentTimeMillis() - start) + "ms.");

			double randomNumber = this.random.nextDouble();
			double sum = 0;
			for (Entry<A, N> entry : actionsWithSuccessors.entrySet()) {
				sum += skills.getDouble(encoder.getIndexOfObject(entry.getValue()));
				if (sum >= randomNumber) {
					//					System.out.println("Selected " + entry.getKey() + " with skill value " + skills.get(entry.getValue()));
					return entry.getKey();
				}
			}
			throw new IllegalStateException("Could not find child among successors.");

		} catch (AlgorithmTimeoutedException | InterruptedException | AlgorithmExecutionCanceledException | AlgorithmException e) {
			throw new ActionPredictionFailedException(e);
		}
	}

	@Override
	public void registerListener(final Object listener) {
		this.eventBus.register(listener);
	}

	@Override
	public void setGraph(final LabeledGraph<N, A> graph) {
		this.preferenceKernel.setExplorationGraph(graph);
	}

	@Override
	public String getLoggerName() {
		return this.logger.getName();
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger = LoggerFactory.getLogger(name);
	}

	@Override
	public void updatePath(final IPath<N, A> path, final Double playout) {
		this.preferenceKernel.signalNewScore(path, playout);
	}

}
