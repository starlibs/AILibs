package DataManager;

import java.util.List;

import jaicore.CustomDataTypes.ProblemInstance;

public interface IInstanceCollector <I>{
	List<ProblemInstance<I>> getProblemInstances();
}
