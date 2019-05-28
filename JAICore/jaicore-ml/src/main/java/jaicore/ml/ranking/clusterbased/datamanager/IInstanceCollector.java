package jaicore.ml.ranking.clusterbased.datamanager;

import java.util.List;

import jaicore.ml.ranking.clusterbased.customdatatypes.ProblemInstance;

public interface IInstanceCollector <I>{
	List<ProblemInstance<I>> getProblemInstances();
}
