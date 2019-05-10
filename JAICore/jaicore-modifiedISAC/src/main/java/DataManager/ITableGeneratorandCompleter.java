package DataManager;

import java.util.List;

import jaicore.CustomDataTypes.ProblemInstance;
import jaicore.CustomDataTypes.Table;

public interface ITableGeneratorandCompleter<I,S,P> {
	Table<I,S,P> getInforamtionforRanking(List<ProblemInstance<I>> InstancesToRank);
}
