package jaicore.basic.algorithm;

import jaicore.basic.ScoredItem;

public abstract class AOptimizer<I, O, U extends ScoredItem<V>, V extends Comparable<V>> extends AAlgorithm<I, O> implements IOptimizationAlgorithm<I, O, U, V> {
	
	private U bestSeenSolution;
	
	public AOptimizer() {
		super();
	}

	
	public AOptimizer(I input) {
		super(input);
	}

	
	/**
	 * Updates the best seen solution if the new solution is better.
	 * Returns true iff the best seen solution has been updated.
	 * 
	 * @param candidate
	 * @return
	 */
	protected boolean updateBestSeenSolution(U candidate) {
		if (bestSeenSolution == null || (candidate.getScore() != null && candidate.getScore().compareTo(bestSeenSolution.getScore()) < 0)) {
			bestSeenSolution = candidate;
			return true;
		}
		return false;
	}

	public U getBestSeenSolution() {
		return bestSeenSolution;
	}

	@Override
	public IOptimizerResult<U, V> getOptimizationResult() {
		return new IOptimizerResult<>(bestSeenSolution, bestSeenSolution.getScore());
	}
	
	public abstract O getOutput();

	@Override
	public O call() throws Exception {
		while (hasNext())
			nextWithException();
		return getOutput();
	}
}
