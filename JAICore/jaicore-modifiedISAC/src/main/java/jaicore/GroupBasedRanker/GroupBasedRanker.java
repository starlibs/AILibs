package jaicore.GroupBasedRanker;

import DataManager.IInstanceCollector;
import DataManager.ITableGeneratorandCompleter;
import jaicore.CustomDataTypes.RankingForGroup;
import jaicore.Ranker.Ranker;

/**
 * @author Helen
 *
 * @param <C> The center of the groups that have rankings
 * @param <I> The problem instances that get grouped and used to find good solutions for them
 * @param <S> Solutions that were tested for problem instances and are getting ranked for 
 * for groups of them
 * @param <P> The performances of the solution for a given Problem instance.
 */
abstract public class GroupBasedRanker<C,I,S,P> implements Ranker<S,I>{
	IInstanceCollector<I> instancecollection;
	ITableGeneratorandCompleter<I,S,P> informationForRankingOfInstances;
	
	public GroupBasedRanker(IInstanceCollector<I> instcoll,ITableGeneratorandCompleter<I,S,P> table){
		this.instancecollection = instcoll;
		this.informationForRankingOfInstances = table;
	}
	
	@Override
	public abstract RankingForGroup<C,S> getRanking(I prob);
	
}
