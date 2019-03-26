package jaicore.search.algorithms.standard.bestfirst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.BusyObjectEvaluator;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.PartiallyFailingObjectEvaluator;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.sets.SetUtil;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.model.other.SearchGraphPath;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.QueenNode;

public class RandomCompletionNodeEvaluatorTester extends TimeAwareNodeEvaluatorTester<RandomCompletionBasedNodeEvaluator<QueenNode, String, Double>> {

	@Test
	public void testThatEvaluationDependsOnSeed() throws NodeEvaluationException, InterruptedException {

		Set<Double> seenScores = new HashSet<>();
		for (int seed = 0; seed < 5; seed++) {
			RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> ne = this.getSeededNodeEvaluator(seed, 10);
			for (Node<QueenNode, Double> node : this.getNodesToTest(ne)) {
				Double score = ne.f(node);
				assertTrue("Score " + score + " has already been seen.", !seenScores.contains(score));
				seenScores.add(score);
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
	public void testDuplicateFreeEnumeration() throws NodeEvaluationException, InterruptedException {

		/* this is cheap, test it several times */
		for (int seed = 0; seed < 5; seed++) {
			List<List<QueenNode>> seenSolutions = new ArrayList<>();
			RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> ne = this.getSeededNodeEvaluator(seed, 20); // draw (up to 20 examples), but only 10 solutions exist
			ne.setLoggerName("testednodeevaluator");
			ne.registerSolutionListener(new Object() {
				@Subscribe
				public void receiveSolution(final EvaluatedSearchSolutionCandidateFoundEvent<QueenNode, String, Double> e) {
					List<QueenNode> solution = e.getSolutionCandidate().getNodes();
					seenSolutions.add(solution);
				}
			});
			Collection<Node<QueenNode, Double>> openNodes = this.getNodesToTest(ne);
			assert openNodes.size() == 5 : "open size should be 2 but is " + openNodes.size();

			/* now collect solutions */
			for (Node<QueenNode, Double> node : openNodes) {
				ne.f(node);
			}

			/* check that all solutions have been enumerated exactly once */
			Set<List<QueenNode>> solutionSet = new HashSet<>(seenSolutions);
			assertEquals("The number of found solutions deviates from the sample size!", 10, seenSolutions.size());
			assertEquals("The items " + SetUtil.intersection(solutionSet, seenSolutions) + " solutions found by the completer.", solutionSet.size(), seenSolutions.size());
		}
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneSampleSuccess() throws NodeEvaluationException, InterruptedException {
		int numSamples = 1;
		for (int seed = 0; seed < 1; seed++) {
			for (int successfulInvocation = 1; successfulInvocation <= numSamples; successfulInvocation++) {
				Set<List<QueenNode>> seenSolutions = new HashSet<>();
				List<Double> seenScores = new ArrayList<>();
				List<Integer> successfulInvocations = new ArrayList<>();
				successfulInvocations.add(successfulInvocation);
				RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> ne = this.getNodeEvaluator(new PartiallyFailingObjectEvaluator<>(successfulInvocations, 0.0), seed, numSamples, -1);
				ne.setLoggerName("testednodeevaluator");
				ne.registerSolutionListener(new Object() {
					@Subscribe
					public void receiveSolution(final EvaluatedSearchSolutionCandidateFoundEvent<QueenNode, String, Double> e) {
						List<QueenNode> solution = e.getSolutionCandidate().getNodes();
						seenSolutions.add(solution);
					}
				});
				for (Node<QueenNode, Double> node : this.getNodesToTest(ne)) {
					try {
						Double score = ne.f(node);
						seenScores.add(score);
					} catch (NoSuchElementException e) {
						/* we expect this to happen for some cases */
					}
				}
				assertEquals("There should be exactly one solution.", 1, seenSolutions.size());
				assertEquals(1, seenScores.size());
			}
		}
	}

	public RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> getNodeEvaluator(final IObjectEvaluator<SearchGraphPath<QueenNode, String>, Double> oe, final int seed, final int numSamples, final int timeoutForNodeEvaluationInMs) {
		IObjectEvaluator<SearchGraphPath<QueenNode, String>, Double> se = new IObjectEvaluator<SearchGraphPath<QueenNode, String>, Double>() {

			@Override
			public Double evaluate(final SearchGraphPath<QueenNode, String> solutionPath) throws AlgorithmTimeoutedException, InterruptedException, ObjectEvaluationFailedException {
				return oe.evaluate(solutionPath);
			}
		};
		return new RandomCompletionBasedNodeEvaluator<>(new Random(seed), numSamples, se, -1, timeoutForNodeEvaluationInMs);
	}

	public RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> getSeededNodeEvaluator(final int seed, final int numSamples) {
		return this.getNodeEvaluator(n -> Math.random(), seed, numSamples, -1);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> getNodeEvaluator() {
		return this.getSeededNodeEvaluator(0, 3);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> getBusyNodeEvaluator() {
		return this.getTimedNodeEvaluator(-1);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> getTimedNodeEvaluator(final int timeoutInMS) {
		return this.getNodeEvaluator(new BusyObjectEvaluator<>(), 0, 3, timeoutInMS);
	}

	@Override
	public Collection<Node<QueenNode, Double>> getNodesToTest(final RandomCompletionBasedNodeEvaluator<QueenNode, String, Double> ne) {
		StandardBestFirst<QueenNode, String, Double> bf = this.getBF(new AlternativeNodeEvaluator<>(n -> 0.0, ne)); // the n -> 0.0 is not really used except for efficient initialization
		bf.setLoggerName("testedalgorithm");
		bf.next();
		bf.next();
		Collection<Node<QueenNode, Double>> nodes = new ArrayList<>();
		nodes.addAll(bf.getOpen());
		return nodes;
	}
}
