package jaicore.basic.algorithm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;

public abstract class SolutionCandidateIteratorTester extends GeneralAlgorithmTester {

	private Logger logger = LoggerFactory.getLogger(SolutionCandidateIteratorTester.class);
	private AlgorithmicProblemReduction<Object, Object, Object, Object> reduction;
	private Map<Object, Object> originalProblemsForReducedProblems = new HashMap<>();
	private Map<Object, Collection<?>> reducedProblemsWithOriginalSolutions = new HashMap<>(); // keys are (possibly reduced) problem inputs, and outputs are original solutions

	@Before
	public <I, O> void loadProblemsAndSolutions() throws InterruptedException {

		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Cannot load problems and solutions, because the thread has been interrupted.");
		}

		/* get problem set */
		@SuppressWarnings("unchecked")
		IAlgorithmTestProblemSet<Object> problemSet = (IAlgorithmTestProblemSet<Object>) this.getProblemSet();

		/* create a single reduction that recurses over all applied reductions */
		IAlgorithmTestProblemSet<Object> current = problemSet;
		while (current instanceof ReductionBasedAlgorithmTestProblemSet) {
			@SuppressWarnings("unchecked")
			ReductionBasedAlgorithmTestProblemSet<Object, Object, Object, Object> castCurrent = ((ReductionBasedAlgorithmTestProblemSet<Object, Object, Object, Object>)current);
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
		Map<I, Collection<O>> problemsWithSolutions = ((IAlgorithmTestProblemSetForSolutionIterators<I, O>)current).getProblemsWithSolutions();
		for (Entry<I, Collection<O>> originalProblemWithSolutions : problemsWithSolutions.entrySet()) {
			Object problem = originalProblemWithSolutions.getKey();
			Object reducedProblem = this.reduction.encodeProblem(problem);
			originalProblemsForReducedProblems.put(reducedProblem, problem);
			this.logger.debug("Converting {} to {}", problem, reducedProblem);
			this.reducedProblemsWithOriginalSolutions.put(reducedProblem, originalProblemWithSolutions.getValue());
		}
	}

	private void solveProblemViaCall(final Entry<Object, Collection<?>> problem, final ISolutionCandidateIterator<Object, Object> algorithm) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		final Collection<?> stillMissingSolutions = new ArrayList<>(problem.getValue());
		final Collection<Object> foundSolutions = new ArrayList<>();
		assertNotNull(algorithm);
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
		}
		algorithm.registerListener(new Object() {
			@Subscribe
			public void receiveSolution(final SolutionCandidateFoundEvent<Object> solutionEvent) {
				Object solution = solutionEvent.getSolutionCandidate();
				Object solutionToOriginalProblem = SolutionCandidateIteratorTester.this.reduction != null ? SolutionCandidateIteratorTester.this.reduction.decodeSolution(solution) : solution;
				assertTrue ("Returned solution " + solution + " converted to original solution " + solutionToOriginalProblem + " is not a solution in the original problem according to ground truth.", stillMissingSolutions.contains(solutionToOriginalProblem) || foundSolutions.contains(solutionToOriginalProblem));
				if (foundSolutions.contains(solutionToOriginalProblem)) {
					SolutionCandidateIteratorTester.this.logger.warn("Returned solution {} converted to original solution {} has already been found earlier, i.e. is returned twice.", solution, solutionToOriginalProblem);
				} else {
					foundSolutions.add(solutionToOriginalProblem);
					stillMissingSolutions.remove(solutionToOriginalProblem);
				}
			}
		});
		while (algorithm.hasNext()) {
			algorithm.nextWithException();
		}
		assertTrue("Found " + foundSolutions.size() + "/" + problem.getValue().size() + " solutions.\n\t" + stillMissingSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t")) + "\nFound solutions: \n\t" + foundSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t")), stillMissingSolutions.isEmpty());
	}

	private void solveProblemViaIterator(final Entry<Object, Collection<?>> problem, final ISolutionCandidateIterator<Object, Object> algorithm) throws AlgorithmTimeoutedException, InterruptedException, AlgorithmExecutionCanceledException, AlgorithmException {
		assertNotNull(algorithm);
		if (algorithm instanceof ILoggingCustomizable) {
			((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
		}
		boolean initialized = false;
		boolean terminated = false;
		final Collection<?> stillMissingSolutions = new ArrayList<>(problem.getValue());
		final Collection<Object> foundSolutions = new ArrayList<>();
		Iterator<AlgorithmEvent> iterator = algorithm.iterator();
		assertNotNull("The search algorithm does return NULL as an iterator for itself.", iterator);
		while (iterator.hasNext()) {
			AlgorithmEvent e = algorithm.nextWithException();
			assertNotNull("The search iterator has returned NULL even though hasNext suggested that more event should come.", e);
			if (!initialized) {
				assertTrue(e instanceof AlgorithmInitializedEvent);
				initialized = true;
			} else if (e instanceof AlgorithmFinishedEvent) {
				terminated = true;
			} else {
				assertTrue(!terminated);
				if (e instanceof SolutionCandidateFoundEvent) {
					Object solution = ((SolutionCandidateFoundEvent<Object>)e).getSolutionCandidate();
					Object solutionToOriginalProblem = solution;
					if (SolutionCandidateIteratorTester.this.reduction != null) {
						solutionToOriginalProblem = SolutionCandidateIteratorTester.this.reduction.decodeSolution(solution);
					}
					if (!stillMissingSolutions.contains(solutionToOriginalProblem)) {
						SolutionCandidateIteratorTester.this.logger.warn("Returned solution {} converted to original solution {} is not a solution in the original problem according to ground truth.", solution, solutionToOriginalProblem);
					} else {
						foundSolutions.add(solutionToOriginalProblem);
						stillMissingSolutions.remove(solutionToOriginalProblem);
					}
				}
			}
		}
		assertTrue("Found " + foundSolutions.size() + "/" + problem.getValue().size() + " solutions. Missing solutions:\n\t" + stillMissingSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t")) + "\nFound solutions: \n\t" + foundSolutions.stream().map(Object::toString).collect(Collectors.joining("\n\t")), stillMissingSolutions.isEmpty());
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmCreationException {
		for (Entry<Object, Collection<?>> problem : this.reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(1);
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			this.solveProblemViaCall(problem, algorithm);
		}
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException, AlgorithmCreationException {
		for (Entry<Object, Collection<?>> problem : this.reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			this.solveProblemViaCall(problem, algorithm);
		}
	}

	@Test
	public void testThatIteratorReturnsEachPossibleSolution() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException {
		for (Entry<Object, Collection<?>> problem : this.reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(1);
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			this.solveProblemViaIterator(problem, algorithm);
		}
	}

	@Test
	public void testThatIteratorReturnsEachPossibleSolutionWithParallelization() throws InterruptedException, AlgorithmTimeoutedException, AlgorithmExecutionCanceledException, AlgorithmException, AlgorithmCreationException {
		for (Entry<Object, Collection<?>> problem : this.reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			this.logger.info("Calling {} to solve problem {}, which as {} solutions.", algorithm.getId(), originalProblemsForReducedProblems.get(problem.getKey()), problem.getValue().size());
			this.solveProblemViaIterator(problem, algorithm);
		}
	}

}
