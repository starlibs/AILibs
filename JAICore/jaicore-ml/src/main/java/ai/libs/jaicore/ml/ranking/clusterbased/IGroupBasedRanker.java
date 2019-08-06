package ai.libs.jaicore.ml.ranking.clusterbased;

import org.api4.java.ai.ml.dataset.IFeatureInstance;
import org.api4.java.ai.ml.dataset.supervised.ranking.IRankingDataset;
import org.api4.java.ai.ml.dataset.supervised.ranking.IRankingInstance;
import org.api4.java.ai.ml.learner.ranker.IRanker;
import org.api4.java.ai.ml.learner.ranker.IRankerConfig;

import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.RankingForGroup;

/**
 * @author Helen
 *
 * @param <Z> The center of the groups that have rankings
 * @param <I> The problem instances that get grouped and used to find good solutions for them
 * @param <O> Solutions that were tested for problem instances and are getting ranked for
 *            for groups of them
 */
public interface IGroupBasedRanker<C extends IRankerConfig, X, O, I extends IFeatureInstance<X> & IRankingInstance<O>, D extends IRankingDataset<X, O, I>, Z> extends IRanker<C, X, O, I, D> {

	public RankingForGroup<Z, O> getRanking(I prob);

}
