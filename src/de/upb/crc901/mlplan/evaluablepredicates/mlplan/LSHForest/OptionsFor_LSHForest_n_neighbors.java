
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LSHForest;
/*
    n_neighbors : int (default = 5)
        Number of neighbors to be returned from query function when
        it is not provided to the :meth:`kneighbors` method.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LSHForest_n_neighbors extends NumericRangeOptionPredicate {
	
	@Override
	protected double getMin() {
		return 1;
	}

	@Override
	protected double getMax() {
		return 1;
	}

	@Override
	protected int getSteps() {
		return -1;
	}

	@Override
	protected boolean needsIntegers() {
		return true;
	}
}

