
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.RidgeClassifier;
/*
    fit_intercept : boolean
        Whether to calculate the intercept for this model. If set to false, no
        intercept will be used in calculations (e.g. data is expected to be
        already centered).


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_RidgeClassifier_fit_intercept extends NumericRangeOptionPredicate {
	
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

