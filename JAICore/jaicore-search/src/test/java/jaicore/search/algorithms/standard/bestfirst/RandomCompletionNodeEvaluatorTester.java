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
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.BusyObjectEvaluator;
import jaicore.basic.IObjectEvaluator;
import jaicore.basic.PartiallyFailingObjectEvaluator;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.exceptions.ObjectEvaluationFailedException;
import jaicore.basic.sets.SetUtil;
import jaicore.search.algorithms.standard.bestfirst.events.EvaluatedSearchSolutionCandidateFoundEvent;
import jaicore.search.algorithms.standard.bestfirst.exceptions.NodeEvaluationException;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.AlternativeNodeEvaluator;
import jaicore.search.algorithms.standard.bestfirst.nodeevaluation.RandomCompletionBasedNodeEvaluator;
import jaicore.search.core.interfaces.ISolutionEvaluator;
import jaicore.search.model.travesaltree.Node;
import jaicore.search.testproblems.nqueens.QueenNode;

public class RandomCompletionNodeEvaluatorTester
		extends TimeAwareNodeEvaluatorTester<RandomCompletionBasedNodeEvaluator<QueenNode, Double>> {

	@Test
	public void testThatEvaluationDependsOnSeed() throws NodeEvaluationException, InterruptedException {

		Set<Double> seenScores = new HashSet<>();
		for (int seed = 0; seed < 5; seed++) {
			RandomCompletionBasedNodeEvaluator<QueenNode, Double> ne = getSeededNodeEvaluator(seed, 10);
			for (Node<QueenNode, Double> node : getNodesToTest(ne)) {
				Double score = ne.f(node);
				assertTrue("Score " + score + " has already been seen.", !seenScores.contains(score));
				seenScores.add(score);
				System.out.println(score);
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
			RandomCompletionBasedNodeEvaluator<QueenNode, Double> ne = getSeededNodeEvaluator(seed, 20); // draw (up to
																											// 20
																											// examples),
																											// but only
																											// 10
																											// solutions
																											// exist
			ne.setLoggerName("testednodeevaluator");
			ne.registerSolutionListener(new Object() {
				@Subscribe
				public void receiveSolution(EvaluatedSearchSolutionCandidateFoundEvent<QueenNode, String, Double> e) {
					List<QueenNode> solution = e.getSolutionCandidate().getNodes();
					seenSolutions.add(solution);
				}
			});
			Collection<Node<QueenNode, Double>> openNodes = getNodesToTest(ne);
			assert openNodes.size() == 5 : "open size should be 2 but is " + openNodes.size();

			/* now collect solutions */
			for (Node<QueenNode, Double> node : openNodes) {
				ne.f(node);
			}

			/* check that all solutions have been enumerated exactly once */
			Set<List<QueenNode>> solutionSet = new HashSet<>(seenSolutions);
			assertEquals("The number of found solutions deviates from the sample size!", 10, seenSolutions.size());
			assertEquals("The items " + SetUtil.intersection(solutionSet, seenSolutions)
					+ " solutions found by the completer.", solutionSet.size(), seenSolutions.size());
		}
	}

	@Test
	public void testThatAScoreIsReturnedIfExactlyOneSampleSuccess()
			throws NodeEvaluationException, InterruptedException {
		int numSamples = 1;
		for (int seed = 0; seed < 1; seed++) {
			for (int successfulInvocation = 1; successfulInvocation <= numSamples; successfulInvocation++) {
				Set<List<QueenNode>> seenSolutions = new HashSet<>();
				List<Double> seenScores = new ArrayList<>();
				List<Integer> successfulInvocations = new ArrayList<>();
				successfulInvocations.add(successfulInvocation);
				RandomCompletionBasedNodeEvaluator<QueenNode, Double> ne = getNodeEvaluator(
						new PartiallyFailingObjectEvaluator<>(successfulInvocations, 0.0), seed, numSamples, -1);
				ne.setLoggerName("testednodeevaluator");
				ne.registerSolutionListener(new Object() {
					@Subscribe
					public void receiveSolution(EvaluatedSearchSolutionCandidateFoundEvent<QueenNode, String, Double> e) {
						List<QueenNode> solution = e.getSolutionCandidate().getNodes();
						seenSolutions.add(solution);
					}
				});
				for (Node<QueenNode, Double> node : getNodesToTest(ne)) {
					try {
						Double score = ne.f(node);
						seenScores.add(score);
					}
					catch (NoSuchElementException e) {
						/* we expect this to happen for some cases */
					}
				}
				assertEquals("There should be exactly one solution.", 1, seenSolutions.size());
				assertEquals(1, seenScores.size());
			}
		}
	}

	@Test
	public void testUsageOfPartialResultsAfterInterrupt() {

	}

	public RandomCompletionBasedNodeEvaluator<QueenNode, Double> getNodeEvaluator(
			IObjectEvaluator<List<QueenNode>, Double> oe, int seed, int numSamples, int timeoutForNodeEvaluationInMs) {
		ISolutionEvaluator<QueenNode, Double> se = new ISolutionEvaluator<QueenNode, Double>() {

			@Override
			public Double evaluateSolution(List<QueenNode> solutionPath) throws InterruptedException, TimeoutException,
					AlgorithmExecutionCanceledException, ObjectEvaluationFailedException {
				return oe.evaluate(solutionPath);
			}

			@Override
			public boolean doesLastActionAffectScoreOfAnySubsequentSolution(List<QueenNode> partialSolutionPath) {
				return true;
			}

			@Override
			public void cancel() {
				// TODO Auto-generated method stub

			}
		};
		return new RandomCompletionBasedNodeEvaluator<QueenNode, Double>(new Random(seed), numSamples, se, -1,
				timeoutForNodeEvaluationInMs);
	}

	public RandomCompletionBasedNodeEvaluator<QueenNode, Double> getSeededNodeEvaluator(int seed, int numSamples) {
		return getNodeEvaluator(n -> Math.random(), seed, numSamples, -1);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<QueenNode, Double> getNodeEvaluator() {
		return getSeededNodeEvaluator(0, 3);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<QueenNode, Double> getBusyNodeEvaluator() {
		return getTimedNodeEvaluator(-1);
	}

	@Override
	public RandomCompletionBasedNodeEvaluator<QueenNode, Double> getTimedNodeEvaluator(int timeoutInMS) {
		return getNodeEvaluator(new BusyObjectEvaluator<>(), 0, 3, timeoutInMS);
	}

	@Override
	public Collection<Node<QueenNode, Double>> getNodesToTest(
			RandomCompletionBasedNodeEvaluator<QueenNode, Double> ne) {
		StandardBestFirst<QueenNode, String, Double> bf = getBF(new AlternativeNodeEvaluator<>(n -> 0.0, ne)); // the n
																												// ->
																												// 0.0
																												// is
																												// not
																												// really
																												// used
																												// except
																												// for
																												// efficient
																												// initialiazion
//		bf.setLoggerName("testedalgorithm");
		bf.next();
		bf.next();
		Collection<Node<QueenNode, Double>> nodes = new ArrayList<>();
		nodes.addAll(bf.getOpen());
		return nodes;
	}
}
