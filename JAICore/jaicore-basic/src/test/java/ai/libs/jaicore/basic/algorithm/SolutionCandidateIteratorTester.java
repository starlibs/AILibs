package ai.libs.jaicore.basic.algorithm;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.api4.java.algorithm.ISolutionCandidateIterator;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.control.ILoggingCustomizable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import ai.libs.jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;

public abstract class SolutionCandidateIteratorTester extends GeneralAlgorithmTester {

	private Logger logger = LoggerFactory.getLogger(SolutionCandidateIteratorTester.class);
	private AlgorithmicProblemReduction<Object, Object, Object, Object> reduction;
	private Map<Object, Object> originalProblemsForReducedProblems = new HashMap<>();

	public <I, O> Map<Object, Collection<?>> loadProblemsAndSolutions(final IAlgorithmTestProblemSet<Object> problemSet) throws InterruptedException {

		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Cannot load problems and solutions, because the thread has been interrupted.");
		}

		/* create a single reduction that recurses over all applied reductions */
		IAlgorithmTestProblemSet<Object> current = problemSet;
		while (current instanceof ReductionBasedAlgorithmTestProblemSet) {
			@SuppressWarnings("unchecked")
			ReductionBasedAlgorithmTestProblemSet<Object, Object, Object, Object> castCurrent = ((ReductionBasedAlgorithmTestProblemSet<Object, Object, Object, Object>) current);
			if (this.reduction == null) {
				this.reduction = castCurrent.getReduction();
			} else {
				this.reduction = new AlgorithmicProblemReduction<Object, Object, Object, Object>() {

					@Override
					public Object encodeProblem(final Object problem) {
						Object intermediateProblem = castCurrent.getReduction().encodeProblem(problem);
						return SolutionCandidateIteratorTester.this.reduction.encodeProblem(intermediateProblem);
					}

					@Override
					public Object decodeSolution(final Object solution) {
						Object intermediateSolution = SolutionCandidateIteratorTester.this.reduction.decodeSolution(solution);
						return castCurrent.getReduction().decodeSolution(intermediateSolution);
					}
				};
			}
			current = castCurrent.getMainProblemSet();
		}

		/* check that the current problem set is really a set for iterating algorithms */
		assert current instanceof IAlgorithmTestProblemSetForSolutionIterators : "The root problem set must be made for iterators.";

		/* retrieve the solutions from the original problem */
		@SuppressWarnings("unchecked")
		Map<I, Collection<O>> problemsWithSolutions = ((IAlgorithmTestProblemSetForSolutionIterators<I, O>) current).getProblemsWithSolutions();
		Map<Object, Collection<?>> reducedProblemsWithOriginalSolutions = new HashMap<>();
		for (Entry<I, Collection<O>> originalProblemWithSolutions : problemsWithSolutions.entrySet()) {
			Object problem = originalProblemWithSolutions.getKey();
			Object reducedProblem = this.reduction.encodeProblem(problem);
			this.originalProblemsForReducedProblems.put(reducedProblem, problem);
			this.logger.debug("Converting {} to {}", problem, reducedProblem);
			reducedProblemsWithOriginalSolutions.put(reducedProblem, originalProblemWithSolutions.getValue());
		}
		return reducedProblemsWithOriginalSolutions;
	}

	private void solveProblemViaCall(final Entry<Object, Collection<?>> problem, final ISolutionCandidateIterator<Object, Object> algorithm)
			throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		final Collection<?> stillMissingSolutions = new ArrayList<>(problem.getValue());
		final Collection<Object> foundSolutions = new ArrayList<>();
		assertNotNull(algorithm);
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
		}
		algorithm.registerListener(new Object() {
			@Subscribe
			public void receiveSolution(final ISolutionCandidateFoundEvent<Object> solutionEvent) {
				assertFalse(Thread.currentThread().isInterrupted(), "The worker thread " + Thread.currentThread() + " has been interrupted while transmitting a solution.");
				Object solution = solutionEvent.getSolutionCandidate();
				Object solutionToOriginalProblem = SolutionCandidateIteratorTester.this.reduction != null ? SolutionCandidateIteratorTester.this.reduction.decodeSolution(solution) : solution;
				assertTrue(problem.getValue().contains(solutionToOriginalProblem), algorithm.getClass() + " has returned solution " + solutionToOriginalProblem + ", which is no solution to the original problem.");
				assertFalse(foundSolutions.contains(solutionToOriginalProblem), algorithm.getClass() + " has returned solution " + solutionToOriginalProblem + " for the second time!");
				foundSolutions.add(solutionToOriginalProblem);
				stillMissingSolutions.remove(solutionToOriginalProblem);
			}
		});
		while (algorithm.hasNext()) {
			algorithm.nextWithException();
		}
		assertTrue(stillMissingSolutions.isEmpty(), "Found " + foundSolutions.size() + "/" + problem.getValue().size() + " solutions.\n\t" + stillMissingSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t")) + "\nFound solutions: \n\t"
				+ foundSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t")));
	}

	private void solveProblemViaIterator(final Entry<Object, Collection<?>> problem, final ISolutionCandidateIterator<Object, Object> algorithm)
			throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		assertNotNull(algorithm);
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
		}
		boolean initialized = false;
		boolean terminated = false;
		final Collection<?> stillMissingSolutions = new ArrayList<>(problem.getValue());
		final Collection<Object> foundSolutions = new ArrayList<>();
		Iterator<IAlgorithmEvent> iterator = algorithm.iterator();
		assertNotNull("The search algorithm does return NULL as an iterator for itself.", iterator);
		while (iterator.hasNext()) {
			IAlgorithmEvent e = algorithm.nextWithException();
			assertNotNull("The search iterator has returned NULL even though hasNext suggested that more event should come.", e);
			if (!initialized) {
				assertTrue(e instanceof AlgorithmInitializedEvent);
				initialized = true;
			} else if (e instanceof AlgorithmFinishedEvent) {
				terminated = true;
			} else {
				assertFalse(terminated);
				if (e instanceof ISolutionCandidateFoundEvent) {
					Object solution = ((ISolutionCandidateFoundEvent<Object>) e).getSolutionCandidate();
					Object solutionToOriginalProblem = solution;
					if (SolutionCandidateIteratorTester.this.reduction != null) {
						solutionToOriginalProblem = SolutionCandidateIteratorTester.this.reduction.decodeSolution(solution);
					}
					assertTrue(problem.getValue().contains(solutionToOriginalProblem), algorithm.getClass() + " has returned solution " + solutionToOriginalProblem + ", which is no solution to the original problem.");
					assertFalse(foundSolutions.contains(solutionToOriginalProblem), algorithm.getClass() + " has returned solution " + solutionToOriginalProblem + " for the second time!");
					foundSolutions.add(solutionToOriginalProblem);
					stillMissingSolutions.remove(solutionToOriginalProblem);
				}
			}
		}
		assertTrue(stillMissingSolutions.isEmpty(), "Found " + foundSolutions.size() + "/" + problem.getValue().size() + " solutions. Missing solutions:\n\t" + stillMissingSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t"))
				+ "\nFound solutions: \n\t" + foundSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t")));
	}

	@ParameterizedTest(name="Single-Thread solution events via bus on {0}")
	@MethodSource("getProblemSets")
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmCreationException {
		Map<Object, Collection<?>> reducedProblemsWithOriginalSolutions = this.loadProblemsAndSolutions(problemSet);
		for (Entry<Object, Collection<?>> problem : reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(1);
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), this.originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			this.solveProblemViaCall(problem, algorithm);
		}
	}

	@ParameterizedTest(name="Multi-Thread solution events via bus on {0}")
	@MethodSource("getProblemSets")
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmCreationException {
		Map<Object, Collection<?>> reducedProblemsWithOriginalSolutions = this.loadProblemsAndSolutions(problemSet);
		for (Entry<Object, Collection<?>> problem : reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), this.originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			this.solveProblemViaCall(problem, algorithm);
		}
	}

	@ParameterizedTest(name="Single-Thread solution events via iterator on {0}")
	@MethodSource("getProblemSets")
	public void testThatIteratorReturnsEachPossibleSolution(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException {
		Map<Object, Collection<?>> reducedProblemsWithOriginalSolutions = this.loadProblemsAndSolutions(problemSet);
		for (Entry<Object, Collection<?>> problem : reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(1);
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), this.originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			this.solveProblemViaIterator(problem, algorithm);
		}
	}

	@ParameterizedTest(name="Single-Thread solution events via iterator on {0}")
	@MethodSource("getProblemSets")
	public void testThatIteratorReturnsEachPossibleSolutionWithParallelization(final IAlgorithmTestProblemSet<Object> problemSet)
			throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException {
		Map<Object, Collection<?>> reducedProblemsWithOriginalSolutions = this.loadProblemsAndSolutions(problemSet);
		for (Entry<Object, Collection<?>> problem : reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), this.originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			this.solveProblemViaIterator(problem, algorithm);
		}
	}

}
