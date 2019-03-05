package jaicore.basic.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

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
import jaicore.basic.algorithm.reduction.AlgorithmicProblemReduction;
import jaicore.basic.sets.SetUtil;

public abstract class SolutionCandidateIteratorTester extends GeneralAlgorithmTester {

	private Logger logger = LoggerFactory.getLogger(SolutionCandidateIteratorTester.class);
	private AlgorithmicProblemReduction<Object, Object, Object, Object> reduction;
	private Map<Object, Collection<?>> reducedProblemsWithOriginalSolutions = new HashMap<>(); // keys are (possibly reduced) problem inputs, and outputs are original solutions

	@Before
	public <I, O> void loadProblemsAndSolutions() throws InterruptedException {

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
			problem = this.reduction.encodeProblem(problem);
			this.reducedProblemsWithOriginalSolutions.put(problem, originalProblemWithSolutions.getValue());
		}
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		for (Entry<Object, Collection<?>> problem : this.reducedProblemsWithOriginalSolutions.entrySet()) {
			final Collection<?> requiredSolutions = problem.getValue();
			final AtomicInteger foundSolutions = new AtomicInteger(0);
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			assertNotNull(algorithm);
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
			}
			algorithm.registerListener(new Object() {
				@Subscribe
				public void receiveSolution(final SolutionCandidateFoundEvent<Object> solutionEvent) {
					Object solution = solutionEvent.getSolutionCandidate();
					Object solutionToOriginalProblem = SolutionCandidateIteratorTester.this.reduction != null ? SolutionCandidateIteratorTester.this.reduction.decodeSolution(solution) : solution;
					if (!requiredSolutions.contains(solutionToOriginalProblem)) {
						SolutionCandidateIteratorTester.this.logger.warn("Returned solution {} converted to original solution {} is not a solution in the original problem according to ground truth.", solution, solutionToOriginalProblem);
					} else {
						foundSolutions.incrementAndGet();
						requiredSolutions.remove(solutionToOriginalProblem);
					}
				}
			});
			algorithm.call();
			assertTrue("Found " + foundSolutions.get() + "/" + problem.getValue().size() + " solutions. Missing solutions: " + requiredSolutions, requiredSolutions.isEmpty());
		}
	}

	@Test
	public void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		for (Entry<Object, Collection<?>> problem : this.reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			assertNotNull(algorithm);
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
			}
			Collection<Object> solutions = new HashSet<>();
			algorithm.registerListener(new Object() {
				@Subscribe
				public void receiveSolution(final SolutionCandidateFoundEvent<Object> solution) {
					solutions.add(solution);
				}
			});
			algorithm.call();
			assertEquals("Found " + solutions.size() + "/" + problem.getValue().size() + " solutions. Missing solutions: " + SetUtil.difference(problem.getValue(), solutions), problem.getValue().size(), solutions.size());
		}
	}

	@Test
	public void testThatIteratorReturnsEachPossibleSolution() throws InterruptedException {
		for (Entry<Object, Collection<?>> problem : this.reducedProblemsWithOriginalSolutions.entrySet()) {
			ISolutionCandidateIterator<Object, Object> algorithm = (ISolutionCandidateIterator<Object, Object>) this.getAlgorithm(problem.getKey());
			assertNotNull(algorithm);
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
			}
			boolean initialized = false;
			boolean terminated = false;
			Collection<Object> solutions = new HashSet<>();
			Iterator<AlgorithmEvent> iterator = algorithm.iterator();
			assertNotNull("The search algorithm does return NULL as an iterator for itself.", iterator);
			while (iterator.hasNext()) {
				AlgorithmEvent e = algorithm.next();
				assertNotNull("The search iterator has returned NULL even though hasNext suggested that more event should come.", e);
				if (!initialized) {
					assertTrue(e instanceof AlgorithmInitializedEvent);
					initialized = true;
				} else if (e instanceof AlgorithmFinishedEvent) {
					terminated = true;
				} else {
					assertTrue(!terminated);
					if (e instanceof SolutionCandidateFoundEvent) {
						assertFalse(solutions.contains(((SolutionCandidateFoundEvent) e).getSolutionCandidate()));
						solutions.add(((SolutionCandidateFoundEvent) e).getSolutionCandidate());
					}
				}
			}
			assertEquals("Found " + solutions.size() + "/" + problem.getValue().size() + " solutions. Missing solutions: " + SetUtil.difference(problem.getValue(), solutions), problem.getValue().size(), solutions.size());
		}
	}

}
