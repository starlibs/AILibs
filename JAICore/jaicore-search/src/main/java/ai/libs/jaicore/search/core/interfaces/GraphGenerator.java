package ai.libs.jaicore.search.core.interfaces;

import ai.libs.jaicore.search.structure.graphgenerator.GoalTester;
import ai.libs.jaicore.search.structure.graphgenerator.RootGenerator;
import ai.libs.jaicore.search.structure.graphgenerator.SuccessorGenerator;

public interface GraphGenerator<T, A> {

	public RootGenerator<T> getRootGenerator();

	public SuccessorGenerator<T, A> getSuccessorGenerator();

	public GoalTester<T> getGoalTester();
}
