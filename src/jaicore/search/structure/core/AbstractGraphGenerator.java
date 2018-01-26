package jaicore.search.structure.core;

import java.util.Random;

public abstract class AbstractGraphGenerator<T,A> implements GraphGenerator<T, A> {
	
	//variables needed for creating ids;
	private boolean nodeNumbering;
	private Random rnd;
	
	public AbstractGraphGenerator() {
		this.nodeNumbering = false;
		this.rnd = new Random();
	}
	
	/**
	 * Method which enables or dissables the nodenumbering and therefore directly influences the id of nodes
	 * @param nodeNumbering
	 * 		<code>true</code> to enable nodenumbering <code>false</code> else
	 */
	public void setNodeNumbering(boolean nodeNumbering) {
		this.nodeNumbering = nodeNumbering;
	}
	
	/**
	 * Creates the next id for a node.
	 * 
	 * The id is a random integer greater 0 if nodenumbering is enabled, otherwise it is -1
	 * @return
	 * 		next id for a node
	 */
	protected int nextID() {
		if(nodeNumbering)
			return rnd.nextInt(Integer.MAX_VALUE);
		else
			return -1;
	}

}
