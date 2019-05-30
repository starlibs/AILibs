package jaicore.basic.aggregate;

import java.util.List;

/**
 * An aggregate function takes a collection of values and returnes a single value representing some kind of aggregation of the collection.
 * In the case of having a collection of reals, this can be for instance the minimum or the maximum.
 *
 * @author mwever
 * @param <D> The domain of the aggregation function. For
 */
public interface IAggregateFunction<D> {

	/**
	 * Aggregates the collection of values to a single value.
	 *
	 * @param values The collection of values to be aggregated.
	 * @return The aggregated value.
	 */
	public D aggregate(List<D> values);

}
