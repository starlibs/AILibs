package jaicore.ml.ranking.clusterbased;

import jaicore.ml.ranking.Ranker;
import jaicore.ml.ranking.clusterbased.customdatatypes.RankingForGroup;

/**
 * @author Helen
 *
 * @param <C> The center of the groups that have rankings
 * @param <I> The problem instances that get grouped and used to find good solutions for them
 * @param <S> Solutions that were tested for problem instances and are getting ranked for
 *            for groups of them
 */
public abstract class GroupBasedRanker<C, I, S> implements Ranker<S, I> {

	@Override
	public abstract RankingForGroup<C, S> getRanking(I prob);

}
