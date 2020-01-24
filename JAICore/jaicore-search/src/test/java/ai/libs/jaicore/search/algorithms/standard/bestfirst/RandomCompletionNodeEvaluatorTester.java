package ai.libs.jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.api4.java.ai.graphsearch.problem.pathsearch.pathevaluation.PathEvaluationException;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.IObjectEvaluator;
import org.api4.java.common.attributedobjects.ObjectEvaluationFailedException;
import org.api4.java.datastructure.graph.ILabeledPath;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.BusyObjectEvaluator;
import ai.libs.jaicore.basic.PartiallyFailingObjectEvaluator;
import ai.libs.jaicore.problems.enhancedttsp.EnhancedTTSPState;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionJobSubmittedEvent;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.exceptions.RCNEPathCompletionFailedException;
import ai.libs.jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import ai.libs.jaicore.search.model.travesaltree.BackPointerPath;

public class RandomCompletionNodeEvaluatorTester extends TimeAwareNodeEvaluatorTester<RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double>> {

	private static final Logger logger = LoggerFactory.getLogger(RandomCompletionNodeEvaluatorTester.class);

	/**
	 * Tests that the random completion evaluation is really random in the sense that changing the seed changes the behavior.
	 *
	 * The test runs the following routine for different seeds: It expands a search graph with BFS up to a maximum node expansion (the number of nodes that shall be evaluated).
	 * For every expanded node, the RCNE is called to identify a set of solution paths, which is associated with the node.
	 * For the different seeds, these paths must differ.
	 *
	 * @throws InterruptedException
	 * @throws AlgorithmTimeoutedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws AlgorithmException
	 * @throws RCNEPathCompletionFailedException
	 */
	@Test
	public void testThatEvaluationDependsOnSeed() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, RCNEPathCompletionFailedException {

		final int NUM_SEEDS = 5;
		final int NUM_NODES = 5;
		final int NUM_SAMPLES = 5;

		final int DIFFICULTY = 10;

		/* create one RCNE for each seed */
		StandardBestFirst<EnhancedTTSPState, String, Double> bf = this.getBF(DIFFICULTY, n -> n.getNodes().size() * 1.0);
		Map<Integer, RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double>> completers = new HashMap<>();
		for (int seed = 0; seed < NUM_SEEDS; seed++) {
			completers.put(seed, this.getSeededNodeEvaluator(DIFFICULTY, seed, 1, 1)); // the number of samples is irrelevant here, because we call the respective method manually
			completers.get(seed).setLoggerName("testednodeevaluator"); // all completers will have the same logger
			completers.get(seed).setGenerator(bf.getGraphGenerator(), bf.getGoalTester());
		}

		/* gather all random completions over different seeds */
		List<List<EnhancedTTSPState>> completionsFoundSoFar = new ArrayList<>();
		while (bf.getExpandedCounter() < NUM_NODES) {
			IAlgorithmEvent e = bf.nextWithException();
			if (e instanceof NodeExpansionJobSubmittedEvent) {

				/* get the expanded node and run all completers on it */
				BackPointerPath<EnhancedTTSPState, String, ?> nodeToEvaluate = ((NodeExpansionJobSubmittedEvent) e).getExpandedNode();
				for (Entry<Integer, RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double>> entry : completers.entrySet()) {
					for (int i = 0; i < NUM_SAMPLES; i++) {
						List<EnhancedTTSPState> completion = entry.getValue().getNextRandomPathCompletionForNode(nodeToEvaluate).getNodes();
						int numCompletionsFoundByNow = completionsFoundSoFar.size();

						/* check that the solution has not been identified by any completer (including itself) earlier. This is a very strong (perhaps unnecessarily and too strong) condition. */
						assertFalse("Solution has been found twice!", completionsFoundSoFar.contains(completion));
						completionsFoundSoFar.add(completion);
						assertEquals(numCompletionsFoundByNow + 1, completionsFoundSoFar.size());
					}
				}
			}
		}
	}

	/**
	 * Tests that the random completer can cope with the situation that it must evaluate a node whose parent has not been evaluated.
	 *
	 * @throws InterruptedException
	 * @throws AlgorithmTimeoutedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws AlgorithmException
	 * @throws PathEvaluationException
	 */
	@Test
	public void testRobustnessOnMissingEvaluations() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {

		final int DIFFICULTY = 10;

		/* create search that is the basis for the analysis (provides the nodes to be analyzed and the graph generator) */
		StandardBestFirst<EnhancedTTSPState, String, Double> bf = this.getBF(DIFFICULTY, n -> n.getNodes().size() * 1.0);
		RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> completer = this.getSeededNodeEvaluator(DIFFICULTY, 0, 1, 1); // the number of samples is irrelevant here, because we call the respective method manually
		completer.setLoggerName("testednodeevaluator");

		/* run completer on every 5-th node found while running BFS */
		while (bf.getExpandedCounter() < 100) {
			IAlgorithmEvent e = bf.nextWithException();
			if (e instanceof NodeExpansionJobSubmittedEvent && bf.getExpandedCounter() % 5 == 0) { // run the RCNE only every 5 node expansions

				/* get the expanded node and run all completers on it */
				BackPointerPath<EnhancedTTSPState, String, ?> nodeToEvaluate = ((NodeExpansionJobSubmittedEvent) e).getExpandedNode();
				assertTrue(completer.evaluate(nodeToEvaluate) >= 0);
			}
		}
	}

	@Test
	/**
	 * This tests over several invocations that all solutions are produced exactly
	 * once.
	 *
	 * This includes, as a special case, that a single call will not produce and
	 * evaluation any solution more than once
	 *
	 * @throws NodeEvaluationException
	 * @throws InterruptedException
	 */
	public void testDuplicateFreeEnumerationOfAllSolutions() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {

		final int NUM_SAMPLES = 10;
		final int CITIES = 4;

		/* create search that is the basis for the analysis (provides the nodes to be analyzed and the graph generator) */
		StandardBestFirst<EnhancedTTSPState, String, Double> bf = this.getBF(CITIES, n -> n.getNodes().size() * 1.0);
		RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> completer = this.getSeededNodeEvaluator(CITIES, 0, 1, 1); // the number of samples is irrelevant here, because we call the respective method manually
		completer.setLoggerName("testednodeevaluator");

		/* collect random completions for different nodes and make sure that no solution is seen twice (similar to the seed independence check) */
		List<List<EnhancedTTSPState>> completionsFoundSoFar = new ArrayList<>();
		while (bf.hasNext()) {
			IAlgorithmEvent e = bf.nextWithException();
			if (e instanceof NodeExpansionJobSubmittedEvent) {

				/* get the expanded node and run all completers on it */
				BackPointerPath<EnhancedTTSPState, String, ?> nodeToEvaluate = ((NodeExpansionJobSubmittedEvent) e).getExpandedNode();
				for (int i = 0; i < NUM_SAMPLES; i++) {
					try {
						List<EnhancedTTSPState> completion = completer.getNextRandomPathCompletionForNode(nodeToEvaluate).getNodes();

						/* check that the solution has not been identified by any completer (including itself) earlier. This is a very strong (perhaps unnecessarily and too strong) condition. */
						assertFalse("Solution has been found twice!", completionsFoundSoFar.contains(completion));
						completionsFoundSoFar.add(completion);
					} catch (RCNEPathCompletionFailedException ex) {
						assertEquals(CombinatoricsUtils.factorial(CITIES - 1), completionsFoundSoFar.size());
					}
				}
			}
		}
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneOutOfOneSampleSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		this.testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 1, 1);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneOutOfTwoSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		this.testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 1, 2);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneOutOfTenSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		this.testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 1, 10);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyTwoOutOfTwoSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		this.testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 2, 2);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyTwoOutOfTenSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {
		this.testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 2, 10);
	}

	public void testThatAScoreIsReturnedIfExactlyKSampleSucceed(final int cities, final int k, final int n) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, PathEvaluationException {

		final int NUM_SEEDS = 5;
		final int NUM_SAMPLES = n;

		/* create search that is the basis for the analysis (provides the nodes to be analyzed and the graph generator) */
		StandardBestFirst<EnhancedTTSPState, String, Double> bf = this.getBF(cities, node -> node.getNodes().size() * 1.0);

		for (int seed = 0; seed < NUM_SEEDS; seed++) {
			for (int lastSuccessfulInvocation = k; lastSuccessfulInvocation <= NUM_SAMPLES; lastSuccessfulInvocation++) { // defines the number of the evaluation under the node that will be successful (all other will fail)
				Set<List<EnhancedTTSPState>> seenSolutions = new HashSet<>();
				List<Double> seenScores = new ArrayList<>();
				List<Integer> successfulInvocations = new ArrayList<>();
				for (int i = lastSuccessfulInvocation - k; i < lastSuccessfulInvocation; i++) {
					successfulInvocations.add(i + 1);
				}
				RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> ne = this.getNodeEvaluator(cities, new PartiallyFailingObjectEvaluator<>(successfulInvocations, 0.0), seed, k, lastSuccessfulInvocation, -1);
				ne.setLoggerName("testednodeevaluator");
				ne.setGenerator(bf.getGraphGenerator(), bf.getGoalTester());
				ne.registerSolutionListener(new Object() {
					@Subscribe
					public void receiveSolution(final EvaluatedSearchSolutionCandidateFoundEvent<EnhancedTTSPState, String, Double> e) {
						List<EnhancedTTSPState> solution = e.getSolutionCandidate().getNodes();
						seenSolutions.add(solution);
						seenScores.add(e.getScore());
					}
				});

				/* now evaluate the root node */
				bf.initGraph();
				BackPointerPath<EnhancedTTSPState, String, ?> root = bf.getOpen().get(0);
				ne.evaluate(root);
				assertEquals("There should be exactly " + k + " solutions.", k, seenSolutions.size());
				assertEquals("There should be exactly " + k + " scores.", k, seenScores.size());
			}
		}
	}

	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> getNodeEvaluator(final int problemDifficulty, final IObjectEvaluator<ILabeledPath<EnhancedTTSPState, String>, Double> oe, final int seed,
			final int numSamples, final int maxSamples, final int timeoutForNodeEvaluationInMs) {

		/* create search that is the basis for the analysis (provides the nodes to be analyzed and the graph generator) */
		StandardBestFirst<EnhancedTTSPState, String, Double> bf = this.getBF(problemDifficulty, n -> n.getNodes().size() * 1.0);

		IObjectEvaluator<ILabeledPath<EnhancedTTSPState, String>, Double> se = new IObjectEvaluator<ILabeledPath<EnhancedTTSPState, String>, Double>() {

			@Override
			public Double evaluate(final ILabeledPath<EnhancedTTSPState, String> solutionPath) throws InterruptedException, ObjectEvaluationFailedException {
				return oe.evaluate(solutionPath);
			}
		};
		RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> rcne = new RandomCompletionBasedNodeEvaluator<>(new Random(seed), numSamples, maxSamples, se, -1, timeoutForNodeEvaluationInMs);
		rcne.setGenerator(bf.getGraphGenerator(), bf.getGoalTester());
		return rcne;
	}

	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> getSeededNodeEvaluator(final int problemDifficulty, final int seed, final int numSamples, final int maxSamples) {
		return this.getNodeEvaluator(problemDifficulty, n -> Math.abs(n.hashCode() * 1.0), seed, numSamples, maxSamples, -1); // maps each solution path to its hash code
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> getNodeEvaluator() {
		return this.getSeededNodeEvaluator(1000, 0, 3, 6);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> getBusyNodeEvaluator() {
		return this.getTimedNodeEvaluator(-1);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPState, String, Double> getTimedNodeEvaluator(final int timeoutInMS) {
		return this.getNodeEvaluator(1000, new BusyObjectEvaluator<>(), 0, 3, 6, timeoutInMS);
	}

	@Override
	public Collection<BackPointerPath<EnhancedTTSPState, String, Double>> getNodesToTestInDifficultProblem(final int numNodes) {
		StandardBestFirst<EnhancedTTSPState, String, Double> bf = this.getBF(100, n -> 0.0);
		bf.next();
		bf.next();
		return bf.getOpen().stream().limit(numNodes).collect(Collectors.toList());
	}
}
