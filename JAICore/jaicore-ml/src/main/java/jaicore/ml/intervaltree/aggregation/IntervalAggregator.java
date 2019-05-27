package jaicore.ml.intervaltree.aggregation;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.oned.Interval;

/**
 * An IntervalAggeregator can aggregate from a list of intervals, more precisely
 * given a list of predictions in the leaf node, it can predict a range.
 *
 * The basic Aggregators that we introduce are based on minimal and maximal
 * values (we call these the <i>Aggressive Predictors</i>) and based on
 * quantiles.
 *
 * @author elppa
 *
 */
public interface IntervalAggregator extends Serializable{

	public Interval aggregate(List<Double> toAggregate);
}
