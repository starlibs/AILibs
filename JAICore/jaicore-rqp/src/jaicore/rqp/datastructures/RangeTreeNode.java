package jaicore.rqp.datastructures;

import java.util.List;
import weka.core.Instance;

public class RangeTreeNode {
	private int dimension;
	private double intervalLowerBound;
	private double intervalUpperBound;
	private RangeTreeNode leftChild;
	private RangeTreeNode rightChild;
	private RangeTree subTree;	
	private boolean isLeaf;
	private List<Instance> storedPoints;
	
	public RangeTreeNode(boolean isLeaf, int dimension, double intervalLowerBound, double intervalUpperBound, List<Instance> storedPoints) {
		super();
		this.isLeaf = isLeaf;
		this.dimension = dimension;
		this.intervalLowerBound = intervalLowerBound;
		this.intervalUpperBound = intervalUpperBound;
		this.leftChild = leftChild;
		this.rightChild = rightChild;
		this.subTree = subTree;
		this.storedPoints = storedPoints;
	}
}
