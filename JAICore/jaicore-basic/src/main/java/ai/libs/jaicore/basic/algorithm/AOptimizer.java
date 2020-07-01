package ai.libs.jaicore.basic.algorithm;

import java.util.NoSuchElementException;

import org.api4.java.algorithm.IOptimizationAlgorithm;
import org.api4.java.algorithm.events.IAlgorithmEvent;
import org.api4.java.algorithm.events.result.ISolutionCandidateFoundEvent;
import org.api4.java.algorithm.exceptions.AlgorithmException;
import org.api4.java.algorithm.exceptions.AlgorithmExecutionCanceledException;
import org.api4.java.algorithm.exceptions.AlgorithmTimeoutedException;
import org.api4.java.common.attributedobjects.ScoredItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.libs.jaicore.basic.IOwnerBasedAlgorithmConfig;

/**
 * The AOptimizer represents an algorithm that is meant to optimize for a single best solution.
 * While it may observe multiple candidates for the best solution and report them via events
 * when running, eventually it will return only the single best observed one.
 *
 * @author fmohr, wever
 *
 * @param <I> The type of input instances (problems) to be solved by the algorithm.
 * @param <O> The type of output that is obtained by running the algorithm on the input.
 * @param <V> The type performance values will have to compare different solutions.
 */
public abstract class AOptimizer<I, O extends ScoredItem<V>, V extends Comparable<V>> extends ASolutionCandidateIterator<I, O> implements IOptimizationAlgorithm<I, O, V> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AOptimizer.class);
	private String loggerName;

	/* currently best solution candidate observed so far */
	private O bestSeenSolution;
	private V bestScoreKnownToExist = null;

	/**
	 * C'tor taking only an input as a parameter.
	 *
	 * @param input The input of the algorithm.
	 */
	public AOptimizer(final I input) {
		super(input);
	}

	/**
	 * C'tor taking a configuration of the algorithm and an input for the algorithm as arguments.
	 *
	 * @param config The parameterization of the algorithm.
	 * @param input The input to the algorithm (the problem to solve).
	 */
	protected AOptimizer(final IOwnerBasedAlgorithmConfig config, final I input) {
		super(config, input);
	}

	/**
	 * Updates the best seen solution if the new solution is better. Returns true iff the best seen solution has been updated.
	 *
	 * @param candidate A candidate for a new best seen solution. It is only updated iff the candidate performs better than the bestSeenSolution observed so far.
	 * @return Returns true iff the candidate is the best seen solution.
	 */
	protected boolean updateBestSeenSolution(final O candidate) {
		assert (candidate != null) : "Cannot update best solution with null.";
		this.tellAboutBestScoreKnownToExist(candidate.getScore());
		if (this.bestSeenSolution == null || (candidate.getScore() != null && candidate.getScore().compareTo(this.bestSeenSolution.getScore()) < 0)) {
			if (this.bestSeenSolution != null) {
				this.logger.info("New best solution found with score={} (old={})", candidate.getScore(), this.bestSeenSolution.getScore());
			} else {
				this.logger.info("First best solution found with score={} ", candidate.getScore());
			}
			this.bestSeenSolution = candidate;
			return true;
		}
		return false;
	}

	/**
	 * Sets the best seen solution regardless the currently best solution.
	 *
	 * @param candidate
	 * @return true iff the new solution has a higher score than the existing one
	 */
	protected boolean setBestSeenSolution(final O candidate) {
		boolean isBetterThanCurrent = (this.bestSeenSolution == null || (candidate.getScore() != null && candidate.getScore().compareTo(this.bestSeenSolution.getScore()) < 0));
		this.tellAboutBestScoreKnownToExist(candidate.getScore());
		this.bestSeenSolution = candidate;
		return isBetterThanCurrent;
	}

	@Override
	public O nextSolutionCandidate() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
		O candidate = super.nextSolutionCandidate();
		this.updateBestSeenSolution(candidate);
		return candidate;
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



	public V getBestScoreKnownToExist() {
		return this.bestScoreKnownToExist;
	}

	public void tellAboutBestScoreKnownToExist(final V bestScoreKnownToExist) {
		if (this.bestScoreKnownToExist == null || bestScoreKnownToExist.compareTo(this.bestScoreKnownToExist) < 0) {
			this.logger.info("Updating best known achievable score to {}.", bestScoreKnownToExist);
			this.bestScoreKnownToExist = bestScoreKnownToExist;
		}
	}

	/**
	 * @return The best seen solution, yet.
	 */
	public O getBestSeenSolution() {
		return this.bestSeenSolution;
	}

	@Override
	public O call() throws InterruptedException, AlgorithmExecutionCanceledException, AlgorithmTimeoutedException, AlgorithmException {
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
