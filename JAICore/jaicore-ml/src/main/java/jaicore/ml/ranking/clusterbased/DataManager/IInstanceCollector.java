package jaicore.ml.ranking.clusterbased.DataManager;

import java.util.List;

import jaicore.ml.ranking.clusterbased.CustomDataTypes.ProblemInstance;

public interface IInstanceCollector <I>{
	List<ProblemInstance<I>> getProblemInstances();
}
