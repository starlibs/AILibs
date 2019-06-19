package ai.libs.jaicore.ml.ranking.clusterbased.datamanager;

import java.util.List;

import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;
import ai.libs.jaicore.ml.ranking.clusterbased.customdatatypes.Table;

public interface ITableGeneratorandCompleter<I, S, P> {
	public Table<I, S, P> getInforamtionforRanking(List<ProblemInstance<I>> instancesToRank);
}
