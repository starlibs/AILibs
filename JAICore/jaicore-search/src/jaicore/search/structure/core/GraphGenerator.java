package jaicore.search.structure.core;

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
	 * indicates if the nodes should be get a unique id, or if all should get the same id od -1
	 * @return
	 * 		<code>true</code> if every node should get an unique id, otherwise return <code>false</code>
	 */
	public  void setNodeNumbering(boolean nodenumbering);
	
	
	
	
}
