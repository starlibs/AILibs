package jaicore.search.structure.graphgenerator;

public interface SelfContained {
	
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
