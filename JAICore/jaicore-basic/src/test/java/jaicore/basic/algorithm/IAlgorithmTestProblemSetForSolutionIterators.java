package jaicore.basic.algorithm;

import java.util.Collection;
import java.util.Map;

public interface IAlgorithmTestProblemSetForSolutionIterators<I, O> extends IAlgorithmTestProblemSet<I> {
	public Map<I, Collection<O>> getProblemsWithSolutions() throws InterruptedException;
}
