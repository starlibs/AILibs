package jaicore.basic.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;
import jaicore.basic.algorithm.exceptions.AlgorithmTimeoutedException;

/**
 * A template for algorithms that iterate over solution candidates. By default,
 * if this algorithm is called, it returns the first solution it finds.
 *
 * @author fmohr
 *
 * @param <I>
 * @param <O>
 */
public abstract class ASolutionCandidateIterator<I, O> extends AAlgorithm<I, O> implements ISolutionCandidateIterator<I, O> {

	public ASolutionCandidateIterator(final I input) {
		super(input);
	}

	protected ASolutionCandidateIterator(final IAlgorithmConfig config,final I input) {
		super(config, input);
	}

	@Override
	public O nextSolutionCandidate() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		SolutionCandidateFoundEvent<O> event = this.nextSolutionCandidateEvent();
		return event.getSolutionCandidate();
	}

	@Override
	public SolutionCandidateFoundEvent<O> nextSolutionCandidateEvent() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while (this.hasNext()) {
			AlgorithmEvent event = this.nextWithException();
			if (event instanceof SolutionCandidateFoundEvent) {
				@SuppressWarnings("unchecked")
				SolutionCandidateFoundEvent<O> castedEvent = (SolutionCandidateFoundEvent<O>) event;
				return castedEvent;
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public O call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		O candidate = this.nextSolutionCandidate();
		this.terminate(); // make sure that a termination event is sent
		return candidate;
	}

	/**
	 * Gathers all solutions that exist
	 *
	 * @return
	 * @throws InterruptedException
	 * @throws AlgorithmExecutionCanceledException
	 * @throws TimeoutException
	 * @throws AlgorithmException
	 */
	public List<O> collectAllSolutions() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		List<O> solutions = new ArrayList<>();
		while (this.hasNext()) {
			solutions.add(this.nextSolutionCandidate());
		}
		return solutions;
	}
}
