package jaicore.search.structure.graphgenerator;

import java.util.List;

public interface PathGoalTester<T> extends GoalTester<T> {

	/**
	 * check if the path is a goal path for the graph
	 * @param path
	 * 				the path to check
	 * @return
	 * 		<code>true</code> if the path is a goal path, <code>false</code> otherwise.
	 */
	public boolean isGoal(List<T> path);
	
}
