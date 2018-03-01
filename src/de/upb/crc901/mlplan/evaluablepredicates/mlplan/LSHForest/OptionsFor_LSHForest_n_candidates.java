
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LSHForest;
/*
    n_candidates : int (default = 50)
        Minimum number of candidates evaluated per estimator, assuming enough
        items meet the `min_hash_match` constraint.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LSHForest_n_candidates extends NumericRangeOptionPredicate {
	
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
		return true;
	}
}

