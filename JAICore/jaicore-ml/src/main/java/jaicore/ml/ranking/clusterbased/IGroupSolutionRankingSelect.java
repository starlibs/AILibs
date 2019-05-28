package jaicore.ml.ranking.clusterbased;

import jaicore.ml.ranking.clusterbased.customdatatypes.Group;
import jaicore.ml.ranking.clusterbased.customdatatypes.RankingForGroup;
import jaicore.ml.ranking.clusterbased.customdatatypes.Table;


/**
 * 
 * @author Helen Beierling
 *
 * @param <C> The identifier of the considered group
 * @param <S> The solutions that are in the group and are getting ranked over all probleminstances
 * the in the group
 * @param <I> The instances in the group
 * @param <P> The performances of the solutions for an probleminstances
 */
public interface IGroupSolutionRankingSelect<C,S,I,P> {
	/**
	 * @param consideredGroup The group for which a ranking is to choose
	 * @param collectInformation The information that was collected for the problem instances in
	 * the group from previous tests.
	 * @return A Ranking of Solutions that performs well for the probleminstances in the group
	 */
	RankingForGroup<C,S> selectGroupsolutionRanking(Group<C,I> consideredGroup,Table<I,S,P>collectInformation);
}
