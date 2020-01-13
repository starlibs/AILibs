package ai.libs.jaicore.ml.ranking.label.learner.clusterbased;

import org.api4.java.ai.ml.ranking.dataset.IRankingDataset;
import org.api4.java.ai.ml.ranking.dataset.IRankingInstance;
import org.api4.java.ai.ml.ranking.learner.IRanker;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.RankingForGroup;

/**
 * @author Helen
 *
 * @param <Z> The center of the groups that have rankings
 * @param <I> The problem instances that get grouped and used to find good solutions for them
 * @param <O> Solutions that were tested for problem instances and are getting ranked for
 *            for groups of them
 */
public interface IGroupBasedRanker<O, I extends IRankingInstance<O>, D extends IRankingDataset<O, I>, Z> extends IRanker<O, I, D> {

	public RankingForGroup<Z, O> getRanking(I prob);

}
