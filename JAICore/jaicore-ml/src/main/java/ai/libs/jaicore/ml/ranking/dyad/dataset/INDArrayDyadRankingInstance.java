package ai.libs.jaicore.ml.ranking.dyad.dataset;

import org.api4.java.ai.ml.ranking.dyad.dataset.IDyadRankingInstance;
import org.nd4j.linalg.api.ndarray.INDArray;

public interface INDArrayDyadRankingInstance extends IDyadRankingInstance {

	/**
	 * Converts a dyad ranking to a {@link INDArray} matrix where each row
	 * corresponds to a dyad.
	 *
	 * @return The dyad ranking in {@link INDArray} matrix form.
	 */
	public INDArray toMatrix();

}
