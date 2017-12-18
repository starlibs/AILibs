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
}
