package jaicore.ml.dyadranking.loss;

import jaicore.ml.dyadranking.dataset.IDyadRankingInstance;

/**
 * Loss function for evaluating dyad rankers.
 * 
 * @author Helena Graf
 *
 */
public interface DyadRankingLossFunction {

	/**
	 * Computes the loss between the actual dyad ordering and predicted dyad
	 * ordering, represented by dyad ranking instances.
	 * 
	 * @param actual
	 *            the correct ordering
	 * @param predicted
	 *            the predicted ordering
	 * @return the loss between the predicted and correct ordering, depending on the
	 *         implementation
	 */
	public double loss(IDyadRankingInstance actual, IDyadRankingInstance predicted);
}
