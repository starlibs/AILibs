package jaicore.basic.algorithm;

import java.util.Collection;
import java.util.Map;

public abstract class AlgorithmTestProblemSetForSolutionIterators<I, O> extends AlgorithmTestProblemSet<I, O> {

	public AlgorithmTestProblemSetForSolutionIterators(String name) {
		super(name);
	}

	public abstract Map<I, Collection<O>> getProblemsWithSolutions();
}
