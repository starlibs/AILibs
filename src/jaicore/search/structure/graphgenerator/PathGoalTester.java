<<<<<<< HEAD
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
=======
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
>>>>>>> fd1c7dd64da900832ded0b2d0dc43f9cb47f6214
