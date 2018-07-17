package jaicore.rqp.util;

import java.util.Comparator;

import weka.core.Instance;

/*
 * Compares two Instance objects based on a given attribute.
 */
public class InstanceAttributeComparator implements Comparator<Instance> {
	private final int dimension;
	
	public InstanceAttributeComparator(int dimension) {
		super();
		this.dimension = dimension;
	}
	
	@Override
	public int compare(Instance instance1, Instance instance2) {
		Double instance1Value = instance1.value(dimension);
		Double instance2Value = instance2.value(dimension);
		return instance1Value.compareTo(instance2Value);
	}		

}
