package jaicore.ml.dyadranking.dataset;

import org.nd4j.linalg.api.ndarray.INDArray;

import jaicore.ml.core.dataset.ILabeledInstance;
import jaicore.ml.dyadranking.Dyad;

/**
 * Represents an instance for a {@link DyadRankingDataset}. A dyad ranking
 * instance contains an ordering of dyads.
 * 
 * @author Helena Graf
 *
 */
public interface IDyadRankingInstance extends ILabeledInstance<IDyadRankingInstance>, Iterable<Dyad> {

	/**
	 * Get the dyad at the specified position in the ordering contained in this
	 * instance.
	 * 
	 * @param position The position in the ordering for which to get the dyad
	 * @return The dyad at the specified position
	 */
	public Dyad getDyadAtPosition(int position);

	/**
	 * Get the number of dyads in the ranking.
	 * 
	 * @return The number of dyads in the ranking.
	 */
	public int length();

	/**
	 * Converts a dyad ranking to a {@link INDArray} matrix where each row
	 * corresponds to a dyad.
	 * 
	 * @return The dyad ranking in {@link INDArray} matrix form.
	 */
	public INDArray toMatrix();
}
