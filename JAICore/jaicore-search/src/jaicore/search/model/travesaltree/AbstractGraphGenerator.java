package jaicore.search.model.travesaltree;

import java.util.Random;

import jaicore.search.core.interfaces.GraphGenerator;

public abstract class AbstractGraphGenerator<T,A> implements GraphGenerator<T, A> {
	
	//variables needed for creating ids;
	private boolean nodeNumbering;
	Random rnd;
	
	public AbstractGraphGenerator() {
		this(1);
	}
	
	/**
	 * Constructor for an AbstractGraphGenerator, which implements versioning, with a given seed.
	 * @param seed
	 * 		The seed for the random generator, which generates the ids.
	 */
	public AbstractGraphGenerator(int seed) {
		this. nodeNumbering = false;
		this.rnd = new Random(seed);
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
	
	/**
	 * Creates a new Random generator with the given seed
	 * @param seed
	 * 		The seed for the random generator.
	 */
			
	public void reset(int seed) {
		this.rnd = new Random(seed);
	}

}
