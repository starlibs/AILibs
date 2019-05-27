package jaicore.ml.ranking.clusterbased.DataManager;

import java.util.List;

import jaicore.ml.ranking.clusterbased.CustomDataTypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.CustomDataTypes.Table;

public interface ITableGeneratorandCompleter<I,S,P> {
	Table<I,S,P> getInforamtionforRanking(List<ProblemInstance<I>> InstancesToRank);
}
