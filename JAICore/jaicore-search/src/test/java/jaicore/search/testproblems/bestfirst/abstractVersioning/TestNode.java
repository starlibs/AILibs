package jaicore.search.testproblems.bestfirst.abstractVersioning;

import jaicore.search.model.travesaltree.AbstractNode;

public class TestNode extends AbstractNode {
	
	private int value;
	
	public TestNode(int v) {
		value = v;
	}
	
	
	public String toString() {
		return "" +value;
	}


	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

}
