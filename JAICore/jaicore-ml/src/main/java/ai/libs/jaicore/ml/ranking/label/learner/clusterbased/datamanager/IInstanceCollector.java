package ai.libs.jaicore.ml.ranking.label.learner.clusterbased.datamanager;

import java.util.List;

import ai.libs.jaicore.ml.ranking.label.learner.clusterbased.customdatatypes.ProblemInstance;

public interface IInstanceCollector <I>{
	List<ProblemInstance<I>> getProblemInstances();
}
