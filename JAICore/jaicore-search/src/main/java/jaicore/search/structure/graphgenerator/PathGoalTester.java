package jaicore.search.structure.graphgenerator;

import java.util.List;

public interface PathGoalTester<T> extends GoalTester<T> {
	
	/**
	 * Check if the current node is a goal for the problem.
	 * 
	 * @param node
	 *            The node to check.
	 * @return <code>true</code> if it is a goal, <code>false</else> otherwise.
	 */
	public boolean isGoal(List<T> node);
}
