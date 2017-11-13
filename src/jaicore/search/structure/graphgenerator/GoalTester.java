package jaicore.search.structure.graphgenerator;

import java.util.List;

import jaicore.search.structure.core.Node;

public interface GoalTester<T> {
	
	/**
	 * Check if the current node is a goal for the problem.
	 * 
	 * @param node
	 *            The node to check.
	 * @return <code>true</code> if it is a goal, <code>false</else> otherwise.
	 */
//	public boolean isGoal(Node<T,?> node);
	
	
	/**
	 * Check if the current path is a goal path for the problem.
	 * 
	 * @param path
	 *				The path to check.
	 * @return <code>true</code> if the path is a goalpath, <code>false</code> else.
	 */
	public boolean isGoal(List<T> path);
}
