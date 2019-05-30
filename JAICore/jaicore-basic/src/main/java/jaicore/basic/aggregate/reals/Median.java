package jaicore.basic.aggregate.reals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * The aggregation function "Median" aggregates the given values with the median operator, thus, returning the median of a list of values.
 *
 * @author mwever
 */
public class Median implements IRealsAggregateFunction {

	@Override
	public Double aggregate(final List<Double> values) {
		if (values.isEmpty()) {
			return Double.NaN;
		}

		List<Double> copyOfValues = new LinkedList<>(values);
		Collections.sort(copyOfValues);

		if (copyOfValues.size() % 2 == 0) {
			int indexL = (values.size() / 2) - 1;
			int indexU = (values.size() / 2);
			return (copyOfValues.get(indexL) + copyOfValues.get(indexU)) / 2;
		} else {
			int index = (values.size() + 1 / 2) - 1;
			return copyOfValues.get(index);
		}
	}
}
