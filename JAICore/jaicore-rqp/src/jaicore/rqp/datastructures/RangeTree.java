package jaicore.rqp.datastructures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jaicore.rqp.util.InstanceAttributeComparator;
import weka.core.Instance;
import weka.core.Instances;

public class RangeTree {
	private RangeTreeNode rootNode;
	private int dimension;
	
	private RangeTree(int dimension, List<List<Instance>> sortedInstanceLists) {
		
	}
	
	public static RangeTree constructRangeTree(Instances instances) {
		// Assume last attribute is the class
		int dimension = instances.numAttributes() - 1;
		// Create sorted list for each dimension
		List<List<Instance>> sortedInstanceLists = new ArrayList<List<Instance>>(dimension);
		for(int i = 0; i < dimension; i++) {
			List<Instance> sortedList = new ArrayList<Instance>(instances.size());
			for(Instance instance : instances) {
				sortedList.add(instance);
			}
			// Comparator for sorting by the i-th attribute
			Comparator<Instance> comparator = new InstanceAttributeComparator(i);
			sortedList.sort(comparator);
			sortedInstanceLists.add(sortedList);
		}
		
		return new RangeTree(dimension, sortedInstanceLists);
	}
}
