package jaicore.basic.algorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jaicore.basic.ScoredItem;

public abstract class AOptimizer<I, O, U extends ScoredItem<V>, V extends Comparable<V>> extends AAlgorithm<I, O> implements IOptimizationAlgorithm<I, O, U, V> {

	/* Logger variables */
	private Logger logger = LoggerFactory.getLogger(AOptimizer.class);
	private String loggerName;

	private U bestSeenSolution;

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
	protected boolean updateBestSeenSolution(final U candidate) {
		if (this.bestSeenSolution == null || (candidate.getScore() != null && candidate.getScore().compareTo(this.bestSeenSolution.getScore()) < 0)) {
			this.bestSeenSolution = candidate;
			return true;
		}
		return false;
	}

	public U getBestSeenSolution() {
		return this.bestSeenSolution;
	}

	@Override
	public IOptimizerResult<U, V> getOptimizationResult() {
		return new IOptimizerResult<>(this.bestSeenSolution, this.bestSeenSolution.getScore());
	}

	public abstract O getOutput();

	@Override
	public O call() throws Exception {
		while (this.hasNext()) {
			this.nextWithException();
		}
		return this.getOutput();
	}

	@Override
	public String getLoggerName() {
		return this.loggerName;
	}

	@Override
	public void setLoggerName(final String name) {
		this.logger.info("Switching logger from {} to {}", this.logger.getName(), name);
		this.logger = LoggerFactory.getLogger(name);
		this.logger.info("Activated logger {} with name {}", name, this.logger.getName());
		super.setLoggerName(this.loggerName + "._algorithm");
	}
}
