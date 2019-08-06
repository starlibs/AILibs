package ai.libs.jaicore.ml.ranking.dyadranking.loss;

import org.api4.java.ai.ml.IRanking;
import org.api4.java.ai.ml.loss.ILossFunction;

import ai.libs.jaicore.ml.ranking.dyadranking.Dyad;

/**
 * Loss fuction specifically tailored to assess the loss in the domain of dyad rankings.
 *
 * @author mwever
 *
 */
public interface IDyadRankingLossFunction extends ILossFunction<IRanking<Dyad>> {

}
