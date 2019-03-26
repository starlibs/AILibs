package hasco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import hasco.core.HASCO;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;
import hasco.serialization.CompositionSerializer;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.util.CycleDetectedResult;
import jaicore.search.util.DeadEndDetectedResult;
import jaicore.search.util.GraphSanityChecker;
import jaicore.search.util.SanityCheckResult;

public abstract class HASCOTester<S extends GraphSearchInput<N, A>, N, A> extends SoftwareConfigurationAlgorithmTester {

	private Logger logger = LoggerFactory.getLogger(HASCOTester.class);

	@Override
	public abstract HASCO<S, N, A, Double> getAlgorithmForSoftwareConfigurationProblem(RefinementConfiguredSoftwareConfigurationProblem<Double> problem);

	@Override
	public SoftwareConfigurationProblemSet getProblemSet() {
		return (SoftwareConfigurationProblemSet) super.getProblemSet();
	}

	private HASCO<S, N, A, Double> getHASCOForSimpleProblem() throws AlgorithmTestProblemSetCreationException {
		return this.getAlgorithmForSoftwareConfigurationProblem(this.getProblemSet().getSimpleProblemInputForGeneralTestPurposes());
	}

	private HASCO<S, N, A, Double> getHASCOForDifficultProblem() throws AlgorithmTestProblemSetCreationException {
		return this.getAlgorithmForSoftwareConfigurationProblem(this.getProblemSet().getDifficultProblemInputForGeneralTestPurposes());
	}

	private HASCO<S, N, A, Double> getHASCOForProblemWithDependencies() throws AlgorithmTestProblemSetCreationException {
		return this.getAlgorithmForSoftwareConfigurationProblem(this.getProblemSet().getDependencyProblemInput());
	}

	private Collection<Pair<HASCO<S, N, A, Double>, Integer>> getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems() throws AlgorithmTestProblemSetCreationException {
		Collection<Pair<HASCO<S, N, A, Double>, Integer>> hascoObjects = new ArrayList<>();
		hascoObjects.add(new Pair<>(this.getHASCOForSimpleProblem(), 6));
		hascoObjects.add(new Pair<>(this.getHASCOForDifficultProblem(), -1));
		hascoObjects.add(new Pair<>(this.getHASCOForProblemWithDependencies(), 12));
		return hascoObjects;
	}

	@Test
	public void sanityCheckOfSearchGraph() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		for (Pair<HASCO<S, N, A, Double>, Integer> pairOfHASCOAndNumOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<S, N, A, Double> hasco = pairOfHASCOAndNumOfSolutions.getX();
			GraphGenerator<N, A> gen = hasco.getGraphGenerator();

			/* check on dead end */
			GraphSanityChecker<N, A> deadEndDetector = new GraphSanityChecker<>(new GraphSearchInput<>(gen), 2000);
			deadEndDetector.setLoggerName("testedalgorithm");
			deadEndDetector.call();
			SanityCheckResult sanity = deadEndDetector.getSanityCheck();
			assertTrue("HASCO graph has a dead end: " + sanity, !(sanity instanceof DeadEndDetectedResult));
			assertTrue("HASCO graph has a cycle: " + sanity, !(sanity instanceof CycleDetectedResult));
		}
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		for (Pair<HASCO<S, N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<S, N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			this.checkNumberOfSolutionOnHASCO(hasco, pairOfHASCOAndExpectedNumberOfSolutions.getY());
		}
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws AlgorithmTestProblemSetCreationException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		for (Pair<HASCO<S, N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<S, N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			hasco.setNumCPUs(Runtime.getRuntime().availableProcessors());
			this.checkNumberOfSolutionOnHASCO(hasco, pairOfHASCOAndExpectedNumberOfSolutions.getY());
		}
	}

	private void checkNumberOfSolutionOnHASCO(final HASCO<S, N, A, Double> hasco, final int numberOfExpectedSolutions) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		if (numberOfExpectedSolutions < 0) {
			return;
		}
		List<ComponentInstance> solutions = new ArrayList<>();
		hasco.registerListener(new Object() {

			@Subscribe
			public void registerSolution(final HASCOSolutionEvent<Double> e) {
				solutions.add(e.getSolutionCandidate().getComponentInstance());
				HASCOTester.this.logger.info("Found solution {}", CompositionSerializer.serializeComponentInstance(e.getSolutionCandidate().getComponentInstance()));
			}
		});
		hasco.call();
		Set<Object> uniqueSolutions = new HashSet<>(solutions);
		assertEquals("Only found " + uniqueSolutions.size() + "/" + numberOfExpectedSolutions + " solutions", numberOfExpectedSolutions, uniqueSolutions.size());
		assertEquals("All " + numberOfExpectedSolutions + " solutions were found, but " + solutions.size() + " solutions were returned in total, i.e. there are solutions returned twice", numberOfExpectedSolutions, solutions.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testThatIteratorReturnsEachPossibleSolution() throws AlgorithmTestProblemSetCreationException {
		for (Pair<HASCO<S, N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<S, N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			int numberOfExpectedSolutions = pairOfHASCOAndExpectedNumberOfSolutions.getY();
			if (numberOfExpectedSolutions < 0) {
				continue;
			}
			List<ComponentInstance> solutions = new ArrayList<>();
			for (AlgorithmEvent e : hasco) {
				if (e instanceof HASCOSolutionEvent) {
					solutions.add(((HASCOSolutionEvent<Double>) e).getSolutionCandidate().getComponentInstance());
					this.logger.info("Found solution {}", CompositionSerializer.serializeComponentInstance(((HASCOSolutionEvent<Double>) e).getSolutionCandidate().getComponentInstance()));
				}
			}
			Set<Object> uniqueSolutions = new HashSet<>(solutions);
			assertEquals("Only found " + uniqueSolutions.size() + "/" + numberOfExpectedSolutions + " solutions", numberOfExpectedSolutions, uniqueSolutions.size());
			assertEquals("All " + numberOfExpectedSolutions + " solutions were found, but " + solutions.size() + " solutions were returned in total, i.e. there are solutions returned twice", numberOfExpectedSolutions, solutions.size());
		}
	}
}
