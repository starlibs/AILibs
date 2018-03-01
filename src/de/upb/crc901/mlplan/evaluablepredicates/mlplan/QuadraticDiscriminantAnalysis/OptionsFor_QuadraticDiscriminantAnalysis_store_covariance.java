
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.QuadraticDiscriminantAnalysis;
/*
    store_covariance : boolean
        If True the covariance matrices are computed and stored in the
        `self.covariance_` attribute.

        .. versionadded:: 0.17


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_QuadraticDiscriminantAnalysis_store_covariance extends NumericRangeOptionPredicate {
	
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

