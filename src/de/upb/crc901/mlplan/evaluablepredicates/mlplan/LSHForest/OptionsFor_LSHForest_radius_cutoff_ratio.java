
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LSHForest;
/*
    radius_cutoff_ratio : float, optional (default = 0.9)
        A value ranges from 0 to 1. Radius neighbors will be searched until
        the ratio between total neighbors within the radius and the total
        candidates becomes less than this value unless it is terminated by
        hash length reaching `min_hash_match`.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LSHForest_radius_cutoff_ratio extends NumericRangeOptionPredicate {
	
	@Override
	protected double getMin() {
		return 0;
	}

	@Override
	protected double getMax() {
		return 0;
	}

	@Override
	protected int getSteps() {
		return 1;
	}

	@Override
	protected boolean needsIntegers() {
		return false;
	}
}

