package util.search.core;

import util.search.graphgenerator.GoalTester;
import util.search.graphgenerator.RootGenerator;
import util.search.graphgenerator.SuccessorGenerator;

public interface GraphGenerator<T, A> {

	public RootGenerator<T> getRootGenerator();

	public SuccessorGenerator<T, A> getSuccessorGenerator();

	public GoalTester<T> getGoalTester();
}
