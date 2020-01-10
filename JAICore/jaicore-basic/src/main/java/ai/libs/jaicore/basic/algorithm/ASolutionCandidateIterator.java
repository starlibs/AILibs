package ai.libs.jaicore.basic.algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import org.api4.java.algorithm.ISolutionCandidateIterator;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;

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

	protected ASolutionCandidateIterator(final IOwnerBasedAlgorithmConfig config,final I input) {
		super(config, input);
	}

	@Override
	public O nextSolutionCandidate() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		ISolutionCandidateFoundEvent<O> event = this.nextSolutionCandidateEvent();
		return event.getSolutionCandidate();
	}

	@Override
	public ISolutionCandidateFoundEvent<O> nextSolutionCandidateEvent() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		while (this.hasNext()) {
			IAlgorithmEvent event = this.nextWithException();
			if (event instanceof ISolutionCandidateFoundEvent) {
				@SuppressWarnings("unchecked")
				ISolutionCandidateFoundEvent<O> castedEvent = (ISolutionCandidateFoundEvent<O>) event;
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
