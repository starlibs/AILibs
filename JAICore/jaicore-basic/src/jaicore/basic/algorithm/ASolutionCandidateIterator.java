package jaicore.basic.algorithm;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

/**
 * A template for algorithms that iterate over solution candidates. By default,
 * if this algorithm is called, it returns the first solution it finds.
 * 
 * @author fmohr
 *
 * @param <I>
 * @param <O>
 */
public abstract class ASolutionCandidateIterator<I, O> extends AAlgorithm<I, O> implements ISolutionCandidateIterator<O> {

	public ASolutionCandidateIterator() {
		super();
	}

	public ASolutionCandidateIterator(final I input) {
		super(input);
	}

	protected ASolutionCandidateIterator(final I input, final IAlgorithmConfig config) {
		super(input, config);
	}

	protected ASolutionCandidateIterator(final IAlgorithmConfig config) {
		super(config);
	}

	public O nextSolutionCandidate() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		SolutionCandidateFoundEvent<O> event = nextSolutionCandidateEvent();
		O candidate = event.getSolutionCandidate();
		return candidate;
	}

	@Override
	public SolutionCandidateFoundEvent<O> nextSolutionCandidateEvent() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		while (hasNext()) {
			AlgorithmEvent event = nextWithException();
			if (event instanceof SolutionCandidateFoundEvent) {
				@SuppressWarnings("unchecked")
				SolutionCandidateFoundEvent<O> castedEvent = (SolutionCandidateFoundEvent<O>) event;
				return castedEvent;
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public O call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		return nextSolutionCandidate();
	}

}
