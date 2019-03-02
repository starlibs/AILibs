package jaicore.basic.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.google.common.eventbus.Subscribe;

import jaicore.basic.ILoggingCustomizable;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.AlgorithmFinishedEvent;
import jaicore.basic.algorithm.events.AlgorithmInitializedEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.sets.SetUtil;

public abstract class SolutionCandidateIteratorTester extends GeneralAlgorithmTester {

	@Override
	public AlgorithmTestProblemSetForSolutionIterators<?, ?> getProblemSet() {
		return (AlgorithmTestProblemSetForSolutionIterators<?,?>) super.getProblemSet();
	}
	
	@Test
	public <I,O> void testThatAnEventForEachPossibleSolutionIsEmittedInSimpleCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		AlgorithmTestProblemSetForSolutionIterators<I,O> problemSet = (AlgorithmTestProblemSetForSolutionIterators<I,O>)getProblemSet();
		Map<I, Collection<O>> problemsWithSolutions = problemSet.getProblemsWithSolutions();
		for (Entry<I, Collection<O>> problem : problemsWithSolutions.entrySet()) {
			ISolutionCandidateIterator<I, O> algorithm = (ISolutionCandidateIterator<I,O>)getAlgorithm(problem.getKey());
			assertNotNull(algorithm);
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
			}
			AtomicInteger seenSolutions = new AtomicInteger(0);
			algorithm.registerListener(new Object() {
				@Subscribe
				public void receiveSolution(SolutionCandidateFoundEvent<O> solution) {
					seenSolutions.incrementAndGet();
				}
			});
			algorithm.call();
			assertEquals("Failed to solve " + problem.getKey() + ". Only found " + seenSolutions.get() + "/" + problem.getValue() + " solutions.", problem.getValue().size(), seenSolutions.get());
		}
	}

	@Test
	public <I,O> void testThatAnEventForEachPossibleSolutionIsEmittedInParallelizedCall() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		AlgorithmTestProblemSetForSolutionIterators<I,O> problemSet = (AlgorithmTestProblemSetForSolutionIterators<I,O>)getProblemSet();
		Map<I, Collection<O>> problemsWithNumbersOfSolutions = problemSet.getProblemsWithSolutions();
		for (Entry<I, Collection<O>> problem : problemsWithNumbersOfSolutions.entrySet()) {
			ISolutionCandidateIterator<I, O> algorithm = (ISolutionCandidateIterator<I,O>)getAlgorithm(problem.getKey());
			algorithm.setNumCPUs(Runtime.getRuntime().availableProcessors());
			assertNotNull(algorithm);
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
			}
			AtomicInteger seenSolutions = new AtomicInteger(0);
			algorithm.registerListener(new Object() {
				@Subscribe
				public void receiveSolution(SolutionCandidateFoundEvent<O> solution) {
					seenSolutions.incrementAndGet();
				}
			});
			algorithm.call();
			assertEquals("Failed to solve " + problem.getKey() + ". Only found " + seenSolutions.get() + "/" + problem.getValue() + " solutions.", problem.getValue().size(), seenSolutions.get());
		}
	}

	@Test
	public <I,O> void testThatIteratorReturnsEachPossibleSolution() {
		AlgorithmTestProblemSetForSolutionIterators<I,O> problemSet = (AlgorithmTestProblemSetForSolutionIterators<I,O>)getProblemSet();
		Map<I, Collection<O>> problemsWithNumbersOfSolutions = problemSet.getProblemsWithSolutions();
		for (Entry<I, Collection<O>> problem : problemsWithNumbersOfSolutions.entrySet()) {
			ISolutionCandidateIterator<I, O> algorithm = (ISolutionCandidateIterator<I,O>)getAlgorithm(problem.getKey());
			assertNotNull(algorithm);
			if (algorithm instanceof ILoggingCustomizable) {
				((ILoggingCustomizable) algorithm).setLoggerName("testedalgorithm");
			}
			System.out.println(problem.getKey() + ": " + problem.getValue());
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
