package jaicore.search.structure.core;

import jaicore.search.structure.graphgenerator.NodeGoalTester;
import jaicore.search.structure.graphgenerator.PathGoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public interface GraphGenerator<T, A> {

	public RootGenerator<T> getRootGenerator();

	public SuccessorGenerator<T, A> getSuccessorGenerator();

//	public GoalTester<T> getGoalTester();

	public PathGoalTester<T> getPathGoalTester();
	
	public NodeGoalTester<T> getNodeGoalTester();
}
