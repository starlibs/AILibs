package ai.libs.jaicore.ml.ranking.label.learner.clusterbased.datamanager;

import java.util.List;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.ProblemInstance;
import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.Table;

public interface ITableGeneratorandCompleter<I, S, P> {
	public Table<I, S, P> getInforamtionforRanking(List<ProblemInstance<I>> instancesToRank);
}
