package jaicore.ml.intervaltree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.DoubleStream;

import com.google.common.collect.Collections2;

public class RQPHelper {

	public enum TREE_AGGREGATION {
		MIN, MAX, AVERAGE, QUANTIL_075
	}

	public double aggregate(TREE_AGGREGATION method, double[] toAggregate) {
		DoubleStream stream = Arrays.stream(toAggregate);
		if (method == TREE_AGGREGATION.AVERAGE) {
			return aggregateAverage(stream);
		}
		if (method == TREE_AGGREGATION.MIN) {
			return aggregateMin(stream);
		}
		if (method == TREE_AGGREGATION.MAX) {
			return aggregateMax(stream);
		}
		if (method == TREE_AGGREGATION.QUANTIL_075) {
			return aggregateQuantil075(Arrays.asList(toAggregate));
		}
		throw new IllegalStateException("Unreachable Statement");
	}

	private double aggregateQuantil075(List<double[]> asList) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double aggregateAverage(DoubleStream toAggregate) {
		return toAggregate.average().orElseThrow(NoSuchElementException::new);
	}

	private double aggregateMin(DoubleStream toAggregate) {
		return toAggregate.min().orElseThrow(NoSuchElementException::new);

	}

	private double aggregateMax(DoubleStream toAggregate) {
		return toAggregate.max().orElseThrow(NoSuchElementException::new);

	}

	private double aggregateQuantil075(double [] toAggregate) {
	//	Collections.sort();
		return 0;
	}
}
