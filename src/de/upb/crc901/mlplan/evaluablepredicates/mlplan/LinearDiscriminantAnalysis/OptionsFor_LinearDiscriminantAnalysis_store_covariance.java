
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LinearDiscriminantAnalysis;
/*
    store_covariance : bool, optional
        Additionally compute class covariance matrix (default False), used
        only in 'svd' solver.

        .. versionadded:: 0.17


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LinearDiscriminantAnalysis_store_covariance extends NumericRangeOptionPredicate {
	
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

