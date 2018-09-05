package jaicore.search.core.interfaces;

import jaicore.search.structure.graphgenerator.GoalTester;
import jaicore.search.structure.graphgenerator.RootGenerator;
import jaicore.search.structure.graphgenerator.SuccessorGenerator;

public interface GraphGenerator<T, A> {

	public RootGenerator<T> getRootGenerator();

	public SuccessorGenerator<T, A> getSuccessorGenerator();

	public GoalTester<T> getGoalTester();
	
	/**
	 * Indicates if the nodes are selfcontained for the solution or if the solution path is needed.
	 * 
	 * @return
	 * 		<code>true</code> if every node contains every information needed for the solution,
	 * 		 <code>false</code> otherwise.
	 * 		
	 */
	public boolean isSelfContained(); 
	
	
	/**
	 * Indicates whether the nodes should get a unique id, or if all should get the same id -1
	 * This is important if one wants to guarantee that the explored graph is expanded as a tree 
	 * 
	 * @return
	 * 		<code>true</code> if every node should get an unique id, otherwise return <code>false</code>
	 */
	public void setNodeNumbering(boolean nodenumbering);
	
	
	
	
}
