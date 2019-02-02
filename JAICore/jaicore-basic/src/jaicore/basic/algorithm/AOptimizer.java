package jaicore.basic.algorithm;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ScoredItem;
import jaicore.basic.algorithm.events.AlgorithmEvent;
import jaicore.basic.algorithm.events.SolutionCandidateFoundEvent;
import jaicore.basic.algorithm.exceptions.AlgorithmException;

public abstract class AOptimizer<I, O extends ScoredItem<V>, V extends Comparable<V>> extends ASolutionCandidateIterator<I, O> implements IOptimizationAlgorithm<I, O, V> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AOptimizer.class);
	private String loggerName;

	private O bestSeenSolution;

	public AOptimizer() {
		super();
	}

	public AOptimizer(final I input) {
		super(input);
	}

	protected AOptimizer(final IAlgorithmConfig config, final I input) {
		super(config, input);
	}

	protected AOptimizer(final IAlgorithmConfig config) {
		super(config);
	}

	/**
	 * Updates the best seen solution if the new solution is better. Returns true iff the best seen solution has been updated.
	 *
	 * @param candidate
	 * @return
	 */
	protected boolean updateBestSeenSolution(final O candidate) {
		if (this.bestSeenSolution == null || (candidate.getScore() != null && candidate.getScore().compareTo(this.bestSeenSolution.getScore()) < 0)) {
			this.bestSeenSolution = candidate;
			return true;
		}
		return false;
	}

	@Override
	public O nextSolutionCandidate() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		O candidate = super.nextSolutionCandidate();
		this.updateBestSeenSolution(candidate);
		return candidate;
	}

	@Override
	public SolutionCandidateFoundEvent<O> nextSolutionCandidateEvent() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
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

	public O getBestSeenSolution() {
		return this.bestSeenSolution;
	}

	@Override
	public O call() throws InterruptedException, AlgorithmExecutionCanceledException, TimeoutException, AlgorithmException {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.bestSeenSolution;
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.loggerName = name;
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._algorithm");
	}
}
