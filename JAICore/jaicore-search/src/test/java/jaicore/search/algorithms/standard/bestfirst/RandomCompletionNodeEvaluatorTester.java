package jaicore.search.algorithms.standard.bestfirst;

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
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.BusyObjectEvaluator;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.PartiallyFailingObjectEvaluator;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.events.NodeExpansionJobSubmittedEvent;
import jaicore.search.algorithms.standard.bestfirst.exceptions.RCNEPathCompletionFailedException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.testproblems.enhancedttsp.EnhancedTTSPNode;

public class RandomCompletionNodeEvaluatorTester extends TimeAwareNodeEvaluatorTester<RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double>> {

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
		StandardBestFirst<EnhancedTTSPNode, String, Double> bf = getBF(DIFFICULTY, n -> n.externalPath().size() * 1.0);
		Map<Integer, RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double>> completers = new HashMap<>();
		for (int seed = 0; seed < NUM_SEEDS; seed++) {
			completers.put(seed, this.getSeededNodeEvaluator(DIFFICULTY, seed, 1, 1)); // the number of samples is irrelevant here, because we call the respective method manually
			completers.get(seed).setLoggerName("testednodeevaluator"); // all completers will have the same logger
			completers.get(seed).setGenerator(bf.getGraphGenerator());
		}

		/* gather all random completions over different seeds */
		List<List<EnhancedTTSPNode>> completionsFoundSoFar = new ArrayList<>();
		while (bf.getExpandedCounter() < NUM_NODES) {
			AlgorithmEvent e = bf.nextWithException();
			if (e instanceof NodeExpansionJobSubmittedEvent) {

				/* get the expanded node and run all completers on it */
				Node<EnhancedTTSPNode, ?> nodeToEvaluate = ((NodeExpansionJobSubmittedEvent) e).getExpandedNode();
				for (Entry<Integer, RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double>> entry : completers.entrySet()) {
					for (int i = 0; i < NUM_SAMPLES; i++) {
						List<EnhancedTTSPNode> completion = entry.getValue().getNextRandomPathCompletionForNode(nodeToEvaluate);
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
	 */
	@Test
	public void testRobustnessOnMissingEvaluations() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {

		final int DIFFICULTY = 10;
		
		/* create search that is the basis for the analysis (provides the nodes to be analyzed and the graph generator) */
		StandardBestFirst<EnhancedTTSPNode, String, Double> bf = getBF(DIFFICULTY, n -> n.externalPath().size() * 1.0);
		RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> completer = this.getSeededNodeEvaluator(DIFFICULTY, 0, 1, 1); // the number of samples is irrelevant here, because we call the respective method manually
		completer.setLoggerName("testednodeevaluator");

		/* run completer on every 5-th node found while running BFS */
		while (bf.getExpandedCounter() < 100) {
			AlgorithmEvent e = bf.nextWithException();
			if (e instanceof NodeExpansionJobSubmittedEvent && bf.getExpandedCounter() % 5 == 0) { // run the RCNE only every 5 node expansions

				/* get the expanded node and run all completers on it */
				Node<EnhancedTTSPNode, ?> nodeToEvaluate = ((NodeExpansionJobSubmittedEvent) e).getExpandedNode();
				assertTrue(completer.f(nodeToEvaluate) >= 0);
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
		StandardBestFirst<EnhancedTTSPNode, String, Double> bf = getBF(CITIES, n -> n.externalPath().size() * 1.0);
		RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> completer = this.getSeededNodeEvaluator(CITIES, 0, 1, 1); // the number of samples is irrelevant here, because we call the respective method manually
		completer.setLoggerName("testednodeevaluator");

		/* collect random completions for different nodes and make sure that no solution is seen twice (similar to the seed independence check) */
		List<List<EnhancedTTSPNode>> completionsFoundSoFar = new ArrayList<>();
		while (bf.hasNext()) {
			AlgorithmEvent e = bf.nextWithException();
			if (e instanceof NodeExpansionJobSubmittedEvent) {

				/* get the expanded node and run all completers on it */
				Node<EnhancedTTSPNode, ?> nodeToEvaluate = ((NodeExpansionJobSubmittedEvent) e).getExpandedNode();
				for (int i = 0; i < NUM_SAMPLES; i++) {
					try {
						List<EnhancedTTSPNode> completion = completer.getNextRandomPathCompletionForNode(nodeToEvaluate);

						/* check that the solution has not been identified by any completer (including itself) earlier. This is a very strong (perhaps unnecessarily and too strong) condition. */
						assertFalse("Solution has been found twice!", completionsFoundSoFar.contains(completion));
						completionsFoundSoFar.add(completion);
					} catch (RCNEPathCompletionFailedException ex) {
						assertEquals(CombinatoricsUtils.factorial(CITIES - 1), (long) completionsFoundSoFar.size());
					}
				}
			}
		}
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneOutOfOneSampleSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 1, 1);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneOutOfTwoSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 1, 2);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneOutOfTenSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 1, 10);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyTwoOutOfTwoSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 2, 2);
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyTwoOutOfTenSamplesSucceeds() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {
		testThatAScoreIsReturnedIfExactlyKSampleSucceed(5, 2, 10);
	}

	public void testThatAScoreIsReturnedIfExactlyKSampleSucceed(int cities, int k, int n) throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException {

		final int NUM_SEEDS = 5;
		final int NUM_SAMPLES = n;

		/* create search that is the basis for the analysis (provides the nodes to be analyzed and the graph generator) */
		StandardBestFirst<EnhancedTTSPNode, String, Double> bf = getBF(cities, node -> node.externalPath().size() * 1.0);

		for (int seed = 0; seed < NUM_SEEDS; seed++) {
			for (int lastSuccessfulInvocation = k; lastSuccessfulInvocation <= NUM_SAMPLES; lastSuccessfulInvocation++) { // defines the number of the evaluation under the node that will be successful (all other will fail)
				Set<List<EnhancedTTSPNode>> seenSolutions = new HashSet<>();
				List<Double> seenScores = new ArrayList<>();
				List<Integer> successfulInvocations = new ArrayList<>();
				for (int i = lastSuccessfulInvocation - k; i < lastSuccessfulInvocation; i++) {
					successfulInvocations.add(i + 1);
				}
				RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> ne = this.getNodeEvaluator(cities, new PartiallyFailingObjectEvaluator<>(successfulInvocations, 0.0), seed, k, lastSuccessfulInvocation, -1);
				ne.setLoggerName("testednodeevaluator");
				ne.setGenerator(bf.getGraphGenerator());
				ne.registerSolutionListener(new Object() {
					@Subscribe
					public void receiveSolution(final EvaluatedSearchSolutionCandidateFoundEvent<EnhancedTTSPNode, String, Double> e) {
						List<EnhancedTTSPNode> solution = e.getSolutionCandidate().getNodes();
						seenSolutions.add(solution);
						seenScores.add(e.getScore());
					}
				});

				/* now evaluate the root node */
				bf.initGraph();
				Node<EnhancedTTSPNode, ?> root = bf.getOpen().get(0);
				ne.f(root);
				assertEquals("There should be exactly " + k + " solutions.", k, seenSolutions.size());
				assertEquals("There should be exactly " + k + " scores.", k, seenScores.size());
			}
		}
	}

	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> getNodeEvaluator(final int problemDifficulty, final IObjectEvaluator<SearchGraphPath<EnhancedTTSPNode, String>, Double> oe, final int seed, final int numSamples, final int maxSamples,
			final int timeoutForNodeEvaluationInMs) {
		
		/* create search that is the basis for the analysis (provides the nodes to be analyzed and the graph generator) */
		StandardBestFirst<EnhancedTTSPNode, String, Double> bf = getBF(problemDifficulty, n -> n.externalPath().size() * 1.0);

		IObjectEvaluator<SearchGraphPath<EnhancedTTSPNode, String>, Double> se = new IObjectEvaluator<SearchGraphPath<EnhancedTTSPNode, String>, Double>() {

			@Override
			public Double evaluate(final SearchGraphPath<EnhancedTTSPNode, String> solutionPath) throws InterruptedException, ObjectEvaluationFailedException {
				return oe.evaluate(solutionPath);
			}
		};
		RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> rcne = new RandomCompletionBasedNodeEvaluator<>(new Random(seed), numSamples, maxSamples, se, -1, timeoutForNodeEvaluationInMs);
		rcne.setGenerator(bf.getGraphGenerator());
		return rcne;
	}

	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> getSeededNodeEvaluator(final int problemDifficulty, final int seed, final int numSamples, final int maxSamples) {
		return this.getNodeEvaluator(problemDifficulty, n -> Math.abs(n.hashCode() * 1.0), seed, numSamples, maxSamples, -1); // maps each solution path to its hash code
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> getNodeEvaluator() {
		return this.getSeededNodeEvaluator(1000, 0, 3, 6);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> getBusyNodeEvaluator() {
		return this.getTimedNodeEvaluator(-1);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<EnhancedTTSPNode, String, Double> getTimedNodeEvaluator(final int timeoutInMS) {
		return this.getNodeEvaluator(1000, new BusyObjectEvaluator<>(), 0, 3, 6, timeoutInMS);
	}

	@Override
	public Collection<Node<EnhancedTTSPNode, Double>> getNodesToTestInDifficultProblem(int numNodes) {
		StandardBestFirst<EnhancedTTSPNode, String, Double> bf = this.getBF(100, n -> 0.0);
		bf.next();
		bf.next();
		return bf.getOpen().stream().limit(numNodes).collect(Collectors.toList());
	}
}
