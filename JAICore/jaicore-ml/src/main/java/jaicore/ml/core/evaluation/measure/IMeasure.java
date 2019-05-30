package jaicore.ml.core.evaluation.measure;

import java.util.List;

import jaicore.basic.aggregate.IAggregateFunction;

/**
 * The interface of a measure which compute a value of O from expected and actual values of I.
 *
 * @author mwever
 *
 * @param <I> The type of the input values (to be measured).
 * @param <O> The type of output values describing the measurement results.
 */
public interface IMeasure<I, O> {

	/**
	 * Computes the measure for a measured input actual and the expected outcome expected.
	 * @param actual The actually available values.
	 * @param expected The expected values to compare the actual values with.
	 * @return The return value of the measure.
	 */
	public O calculateMeasure(I actual, I expected);

	/**
	 * Computes the measure for a lists of input actual and the expected outcome expected.
	 * @param actual The list of actually available values.
	 * @param expected The list of expected values to compare the actual values with.
	 * @return The list of return values of the measure.
	 */
	public List<O> calculateMeasure(List<I> actual, List<I> expected);

	/**
	 * Computes the measure for lists of input actual and the expected outcome expected and aggregates the measured values with the given aggregation.
	 * @param actual The list of actually available values.
	 * @param expected The list of expected values to compare the actual values with.
	 * @param aggregateFunction The aggregate function to be used to aggregate all the measurements.
	 * @return The aggregated return value of the measure.
	 */
	public O calculateMeasure(List<I> actual, List<I> expected, IAggregateFunction<O> aggregateFunction);

	/**
	 * Computes the measure for lists of input actual and the expected outcome expected and aggregates the measured values with the mean, as this is the most frequently used aggregate function.
	 * @param actual The list of actually available values.
	 * @param expected The list of expected values to compare the actual values with.
	 * @param aggregateFunction The aggregate function to be used to aggregate all the measurements.
	 * @return The mean of return values as output by the instance-wise measure.
	 */
	public O calculateAvgMeasure(List<I> actual, List<I> expected);

}
