<<<<<<< HEAD
package jaicore.search.structure.graphgenerator;

import jaicore.search.structure.core.Node;

public interface NodeGoalTester<T> extends GoalTester<T> {

	/**
	 * Check if the current node is a goal for the problem.
	 * 
	 * @param node
	 *            The node to check.
	 * @return <code>true</code> if it is a goal, <code>false</else> otherwise.
	 */
	public boolean isGoal(Node<T,?> node);
	
}
=======
package jaicore.search.structure.graphgenerator;

public interface NodeGoalTester<T> extends GoalTester<T> {
	
	/**
	 * Check if the current node is a goal for the problem.
	 * 
	 * @param node
	 *            The node to check.
	 * @return <code>true</code> if it is a goal, <code>false</else> otherwise.
	 */
	public boolean isGoal(T node);
}
>>>>>>> fd1c7dd64da900832ded0b2d0dc43f9cb47f6214
