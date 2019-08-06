package ai.libs.jaicore.ml.ranking.dyadranking.dataset;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ranking.IRankingInstance;
import org.nd4j.linalg.api.ndarray.INDArray;

import ai.libs.jaicore.ml.ranking.dyadranking.Dyad;

/**
 * Represents an instance for a {@link DyadRankingDataset}. A dyad ranking
 * instance contains an ordering of dyads.
 *
 * @author helegraf
 * @author mwever
 *
 */
public interface IDyadRankingInstance extends IFeatureInstance<Dyad>, IRankingInstance<Dyad> {

	/**
	 * Converts a dyad ranking to a {@link INDArray} matrix where each row
	 * corresponds to a dyad.
	 *
	 * @return The dyad ranking in {@link INDArray} matrix form.
	 */
	public INDArray toMatrix();

}
