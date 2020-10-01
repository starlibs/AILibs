package ai.libs.hasco.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.eventbus.Subscribe;

import ai.libs.hasco.core.HASCO;
import ai.libs.hasco.core.events.HASCOSolutionEvent;
import ai.libs.jaicore.basic.FileUtil;
import ai.libs.jaicore.basic.algorithm.AlgorithmTestProblemSetCreationException;
import ai.libs.jaicore.basic.sets.Pair;
import ai.libs.jaicore.components.api.IComponent;
import ai.libs.jaicore.components.api.IComponentRepository;
import ai.libs.jaicore.components.model.ComponentInstance;
import ai.libs.jaicore.components.model.RefinementConfiguredSoftwareConfigurationProblem;
import ai.libs.jaicore.components.serialization.ComponentSerialization;
import ai.libs.jaicore.logging.LoggerUtil;
import ai.libs.jaicore.search.probleminputs.GraphSearchWithPathEvaluationsInput;
import ai.libs.jaicore.search.util.CycleDetectedResult;
import ai.libs.jaicore.search.util.DeadEndDetectedResult;
import ai.libs.jaicore.search.util.GraphSanityChecker;
import ai.libs.jaicore.search.util.SanityCheckResult;
import ai.libs.jaicore.test.LongParameterizedTest;
import ai.libs.jaicore.test.MediumParameterizedTest;

public abstract class HASCOTester<S extends GraphSearchWithPathEvaluationsInput<N, A, Double>, N, A> extends SoftwareConfigurationAlgorithmTester {

	private ComponentSerialization serializer = new ComponentSerialization(this.getLoggerName() + ".serialization");

	public static Stream<Arguments> getAllCompositionProblems() {
		Stream<File> relevantFiles = FileUtil.getFilesOfFolder(new File("../../../JAICore/jaicore-components/testrsc")).stream();
		ComponentSerialization serializer = new ComponentSerialization(LoggerUtil.LOGGER_NAME_TESTER);
		return relevantFiles.filter(f -> f.isFile() && f.getName().endsWith(".json")).map(f -> {
			try {
				IComponentRepository repository = serializer.deserializeRepository(f);
				if (repository.isEmpty()) {
					return null;
				}
				IComponent firstComponent = repository.iterator().next();
				return Arguments.of(f.getName(), new RefinementConfiguredSoftwareConfigurationProblem<>(f, firstComponent.getProvidedInterfaces().iterator().next(), x -> 0.0));
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}).filter(Objects::nonNull);
	}

	@Override
	public abstract HASCO<N, A, Double> getAlgorithmForSoftwareConfigurationProblem(RefinementConfiguredSoftwareConfigurationProblem<Double> problem);

	private HASCO<N, A, Double> getHASCOForSimpleProblem(final SoftwareConfigurationProblemSet problemSet) throws AlgorithmTestProblemSetCreationException {
		HASCO<N, A, Double> hasco = this.getAlgorithmForSoftwareConfigurationProblem(problemSet.getSimpleProblemInputForGeneralTestPurposes());
		hasco.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		return hasco;
	}

	private HASCO<N, A, Double> getHASCOForDifficultProblem(final SoftwareConfigurationProblemSet problemSet) throws AlgorithmTestProblemSetCreationException {
		HASCO<N, A, Double> hasco = this.getAlgorithmForSoftwareConfigurationProblem(problemSet.getDifficultProblemInputForGeneralTestPurposes());
		hasco.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		return hasco;
	}

	private HASCO<N, A, Double> getHASCOForProblemWithDependencies(final SoftwareConfigurationProblemSet problemSet) throws AlgorithmTestProblemSetCreationException {
		HASCO<N, A, Double> hasco = this.getAlgorithmForSoftwareConfigurationProblem(problemSet.getDependencyProblemInput());
		hasco.setLoggerName(LoggerUtil.LOGGER_NAME_TESTEDALGORITHM);
		return hasco;
	}

	private Collection<Pair<HASCO<N, A, Double>, Integer>> getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems(final SoftwareConfigurationProblemSet problemSet) throws AlgorithmTestProblemSetCreationException {
		Collection<Pair<HASCO<N, A, Double>, Integer>> hascoObjects = new ArrayList<>();
		hascoObjects.add(new Pair<>(this.getHASCOForSimpleProblem(problemSet), 6));
		hascoObjects.add(new Pair<>(this.getHASCOForDifficultProblem(problemSet), -1));
		hascoObjects.add(new Pair<>(this.getHASCOForProblemWithDependencies(problemSet), 17));
		return hascoObjects;
	}

	@MediumParameterizedTest
	@MethodSource("getProblemSets")
	public void sanityCheckOfSearchGraph(final SoftwareConfigurationProblemSet problemSet)
			throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		for (Pair<HASCO<N, A, Double>, Integer> pairOfHASCOAndNumOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems(problemSet)) {
			HASCO<N, A, Double> hasco = pairOfHASCOAndNumOfSolutions.getX();

			/* check on dead end */
			GraphSanityChecker<N, A> deadEndDetector = new GraphSanityChecker<>(hasco.getSearch().getInput(), 2000);
			deadEndDetector.setLoggerName("testedalgorithm");
			deadEndDetector.call();
			SanityCheckResult sanity = deadEndDetector.getSanityCheck();
			assertTrue("HASCO graph has a dead end: " + sanity, !(sanity instanceof DeadEndDetectedResult));
			assertTrue("HASCO graph has a cycle: " + sanity, !(sanity instanceof CycleDetectedResult));
		}
	}

	@LongParameterizedTest
	@MethodSource("getProblemSets")
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall(final SoftwareConfigurationProblemSet problemSet)
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmTestProblemSetCreationException {
		for (Pair<HASCO<N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems(problemSet)) {
			HASCO<N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			this.checkNumberOfSolutionOnHASCO(hasco, pairOfHASCOAndExpectedNumberOfSolutions.getY());
		}
	}

	@LongParameterizedTest
	@MethodSource("getProblemSets")
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall(final SoftwareConfigurationProblemSet problemSet)
			throws AlgorithmTestProblemSetCreationException, InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		for (Pair<HASCO<N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems(problemSet)) {
			HASCO<N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			hasco.setNumCPUs(Runtime.getRuntime().availableProcessors());
			this.checkNumberOfSolutionOnHASCO(hasco, pairOfHASCOAndExpectedNumberOfSolutions.getY());
		}
	}

	private void checkNumberOfSolutionOnHASCO(final HASCO<N, A, Double> hasco, final int numberOfExpectedSolutions) throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		if (numberOfExpectedSolutions < 0) {
			return;
		}
		List<ComponentInstance> solutions = new ArrayList<>();
		hasco.registerListener(new Object() {

			@Subscribe
			public void registerSolution(final HASCOSolutionEvent<Double> e) {
				solutions.add(e.getSolutionCandidate().getComponentInstance());
				HASCOTester.this.logger.info("Found solution {}", HASCOTester.this.serializer.serialize(e.getSolutionCandidate().getComponentInstance()));
			}
		});
		hasco.call();
		Set<Object> uniqueSolutions = new HashSet<>(solutions);
		assertEquals("Only found " + uniqueSolutions.size() + "/" + numberOfExpectedSolutions + " solutions", numberOfExpectedSolutions, uniqueSolutions.size());
		assertEquals("All " + numberOfExpectedSolutions + " solutions were found, but " + solutions.size() + " solutions were returned in total, i.e. there are solutions returned twice", numberOfExpectedSolutions, solutions.size());
	}

	@LongParameterizedTest
	@MethodSource("getProblemSets")
	public void testThatIteratorReturnsEachPossibleSolution(final SoftwareConfigurationProblemSet problemSet) throws AlgorithmTestProblemSetCreationException {
		for (Pair<HASCO<N, A, Double>, Integer> pairOfHASCOAndExpectedNumberOfSolutions : this.getAllHASCOObjectsWithExpectedNumberOfSolutionsForTheKnownProblems(problemSet)) {
			HASCO<N, A, Double> hasco = pairOfHASCOAndExpectedNumberOfSolutions.getX();
			int numberOfExpectedSolutions = pairOfHASCOAndExpectedNumberOfSolutions.getY();
			this.logger.info("Starting HASCO on problem {} with {} solutions.", hasco, numberOfExpectedSolutions);
			if (numberOfExpectedSolutions < 0) {
				continue;
			}
			List<ComponentInstance> solutions = new ArrayList<>();
			for (IAlgorithmEvent e : hasco) {
				if (e instanceof HASCOSolutionEvent) {
					solutions.add(((HASCOSolutionEvent<Double>) e).getSolutionCandidate().getComponentInstance());
					this.logger.info("Found solution {}", this.serializer.serialize(((HASCOSolutionEvent<Double>) e).getSolutionCandidate().getComponentInstance()));
				}
			}
			this.logger.info("Finished HASCO, now evaluating numbers of found solutions.");
			Set<Object> uniqueSolutions = new HashSet<>(solutions);
			assertEquals(numberOfExpectedSolutions, uniqueSolutions.size(), "Only found " + uniqueSolutions.size() + "/" + numberOfExpectedSolutions + " solutions");
			assertEquals(numberOfExpectedSolutions, solutions.size(), "All " + numberOfExpectedSolutions + " solutions were found, but " + solutions.size() + " solutions were returned in total, i.e. there are solutions returned twice");
		}

	}
}
