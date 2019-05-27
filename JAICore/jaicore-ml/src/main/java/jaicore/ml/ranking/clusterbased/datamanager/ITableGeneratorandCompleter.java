package jaicore.ml.ranking.clusterbased.datamanager;

import java.util.List;

import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import jaicore.ml.ranking.clusterbased.customdatatypes.Table;

public interface ITableGeneratorandCompleter<I, S, P> {
	public Table<I, S, P> getInforamtionforRanking(List<ProblemInstance<I>> instancesToRank);
}
