
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LSHForest;
/*
    radius : float, optinal (default = 1.0)
        Radius from the data point to its neighbors. This is the parameter
        space to use by default for the :meth:`radius_neighbors` queries.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LSHForest_radius extends NumericRangeOptionPredicate {
	
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
		return false;
	}
}

