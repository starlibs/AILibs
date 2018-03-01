
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.RidgeClassifier;
/*
    copy_X : boolean, optional, default True
        If True, X will be copied; else, it may be overwritten.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_RidgeClassifier_copy_X extends NumericRangeOptionPredicate {
	
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

