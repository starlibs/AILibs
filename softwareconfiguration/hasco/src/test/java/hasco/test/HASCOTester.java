package hasco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import hasco.core.HASCO;
import hasco.core.HASCOFactory;
import hasco.core.HASCOSolutionCandidate;
import hasco.core.RefinementConfiguredSoftwareConfigurationProblem;
import hasco.events.HASCOSolutionEvent;
import hasco.model.ComponentInstance;
import hasco.serialization.CompositionSerializer;
import hasco.serialization.UnresolvableRequiredInterfaceException;
import jaicore.basic.algorithm.AlgorithmExecutionCanceledException;
import jaicore.basic.algorithm.GeneralAlgorithmTester;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.basic.sets.SetUtil.Pair;
import jaicore.search.core.interfaces.GraphGenerator;
import jaicore.search.probleminputs.GraphSearchInput;
import jaicore.search.util.CycleDetectedResult;
import jaicore.search.util.DeadEndDetectedResult;
import jaicore.search.util.GraphSanityChecker;
import jaicore.search.util.SanityCheckResult;

public abstract class HASCOTester<ISearch extends GraphSearchInput<N, A>, N, A>
		extends GeneralAlgorithmTester<RefinementConfiguredSoftwareConfigurationProblem<Double>, RefinementConfiguredSoftwareConfigurationProblem<Double>, HASCOSolutionCandidate<Double>> {

	private Logger logger = LoggerFactory.getLogger(HASCOTester.class);
	
	private HASCO<ISearch, N, A, Double> getHASCOForProblem(RefinementConfiguredSoftwareConfigurationProblem<Double> problem) {
		HASCOFactory<ISearch, N, A, Double> factory = getFactory();
		factory.setProblemInput(problem);
		HASCO<ISearch, N, A, Double> hasco = factory.getAlgorithm();
		hasco.setTimeout(86400, TimeUnit.SECONDS);
		return hasco;
	}

	private HASCO<ISearch, N, A, Double> getHASCOForSimpleProblem() throws UnresolvableRequiredInterfaceException, IOException {
		return getHASCOForProblem(getSimpleProblemInputForGeneralTestPurposes());
	}

	private HASCO<ISearch, N, A, Double> getHASCOForDifficultProblem() throws IOException {
		return getHASCOForProblem(getDifficultProblemInputForGeneralTestPurposes());
	}

	private HASCO<ISearch, N, A, Double> getHASCOForProblemWithDependencies() throws UnresolvableRequiredInterfaceException, IOException  {
		return getHASCOForProblem(getDependencyProblemInput());
	}

	private Collection<Pair<HASCO<ISearch, N, A, Double>, Integer>> getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems() throws UnresolvableRequiredInterfaceException, IOException {
		Collection<Pair<HASCO<ISearch, N, A, Double>, Integer>> hascoObjects = new ArrayList<>();
		hascoObjects.add(new Pair<>(getHASCOForSimpleProblem(), 6));
		hascoObjects.add(new Pair<>(getHASCOForDifficultProblem(), -1));
		hascoObjects.add(new Pair<>(getHASCOForProblemWithDependencies(), 12));
		return hascoObjects;
	}

	@Test
	public void sanityCheckOfSearchGraph() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, UnresolvableRequiredInterfaceException, IOException  {
		for (Pair<HASCO<ISearch, N, A, Double>, Integer> pairOfHASCOAndNumOfSolutions : getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<ISearch, N, A, Double> hasco = pairOfHASCOAndNumOfSolutions.getX();
			GraphGenerator<N, A> gen = hasco.getGraphGenerator();

			/* check on dead end */
			GraphSanityChecker<N, A> deadEndDetector = new GraphSanityChecker<>(new GraphSearchInput<>(gen), 2000);
			// new VisualizationWindow<>(deadEndDetector).setTooltipGenerator(n -> TFD);
			deadEndDetector.setLoggerName("testedalgorithm");
			deadEndDetector.call();
			SanityCheckResult sanity = deadEndDetector.getSanityCheck();
			assertTrue("HASCO graph has a dead end: " + sanity, !(sanity instanceof DeadEndDetectedResult));
			assertTrue("HASCO graph has a cycle: " + sanity, !(sanity instanceof CycleDetectedResult));
		}
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws Exception {
		for (Pair<HASCO<ISearch, N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<ISearch, N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			checkNumberOfSolutionOnHASCO(hasco, pairOfHASCOAndExpectedNumberOfSolutions.getY());
		}
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws Exception {
		for (Pair<HASCO<ISearch, N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<ISearch, N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			hasco.setNumCPUs(Runtime.getRuntime().availableProcessors());
			checkNumberOfSolutionOnHASCO(hasco, pairOfHASCOAndExpectedNumberOfSolutions.getY());
		}
	}
	
	private void checkNumberOfSolutionOnHASCO(HASCO<ISearch, N, A, Double> hasco, int numberOfExpectedSolutions) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
			if (numberOfExpectedSolutions < 0)
				return;
			List<ComponentInstance> solutions = new ArrayList<>();
			hasco.registerListener(new Object() {
				
				@Subscribe
				public void registerSolution(HASCOSolutionEvent<Double> e) {
					solutions.add(((HASCOSolutionCandidate<Double>) e.getSolutionCandidate()).getComponentInstance());
					logger.info("Found solution {}", CompositionSerializer.serializeComponentInstance(((HASCOSolutionCandidate<Double>) ((HASCOSolutionEvent<Double>) e).getSolutionCandidate()).getComponentInstance()));
				}
			});
			hasco.call();
			Set<Object> uniqueSolutions = new HashSet<>(solutions);
			assertEquals("Only found " + uniqueSolutions.size() + "/" + numberOfExpectedSolutions + " solutions", numberOfExpectedSolutions, uniqueSolutions.size());
			assertEquals("All " + numberOfExpectedSolutions + " solutions were found, but " + solutions.size() + " solutions were returned in total, i.e. there are solutions returned twice",
					numberOfExpectedSolutions, solutions.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testThatIteratorReturnsEachPossibleSolution() throws Exception {
		for (Pair<HASCO<ISearch, N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems()) {
			HASCO<ISearch, N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			int numberOfExpectedSolutions = pairOfHASCOAndExpectedNumberOfSolutions.getY();
			if (numberOfExpectedSolutions < 0)
				continue;
			List<ComponentInstance> solutions = new ArrayList<>();
			for (AlgorithmEvent e : hasco) {
				if (e instanceof HASCOSolutionEvent) {
					solutions.add(((HASCOSolutionCandidate<Double>) ((HASCOSolutionEvent<Double>) e).getSolutionCandidate()).getComponentInstance());
					logger.info("Found solution {}", CompositionSerializer.serializeComponentInstance(((HASCOSolutionCandidate<Double>) ((HASCOSolutionEvent<Double>) e).getSolutionCandidate()).getComponentInstance()));
				}
			}
			Set<Object> uniqueSolutions = new HashSet<>(solutions);
			assertEquals("Only found " + uniqueSolutions.size() + "/" + numberOfExpectedSolutions + " solutions", numberOfExpectedSolutions, uniqueSolutions.size());
			assertEquals("All " + numberOfExpectedSolutions + " solutions were found, but " + solutions.size() + " solutions were returned in total, i.e. there are solutions returned twice",
					numberOfExpectedSolutions, solutions.size());
		}
	}

	public abstract HASCOFactory<ISearch, N, A, Double> getFactory();

	@Override
	public AlgorithmicProblemReduction<RefinementConfiguredSoftwareConfigurationProblem<Double>, RefinementConfiguredSoftwareConfigurationProblem<Double>> getProblemReducer() {
		return p -> p;
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getSimpleProblemInputForGeneralTestPurposes() throws UnresolvableRequiredInterfaceException, IOException {
		return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/simpleproblem.json"), "IFace", n -> 0.0);
	}

	public RefinementConfiguredSoftwareConfigurationProblem<Double> getDependencyProblemInput() throws UnresolvableRequiredInterfaceException, IOException  {
		return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/problemwithdependencies.json"), "IFace", n -> 0.0);
	}

	@Override
	public RefinementConfiguredSoftwareConfigurationProblem<Double> getDifficultProblemInputForGeneralTestPurposes() throws IOException {
		return new RefinementConfiguredSoftwareConfigurationProblem<>(new File("testrsc/difficultproblem.json"), "IFace", n -> 0.0);
	};
}
