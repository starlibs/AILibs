
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.PassiveAggressiveClassifier;
/*
    C : float
        Maximum step size (regularization). Defaults to 1.0.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_PassiveAggressiveClassifier_C extends NumericRangeOptionPredicate {
	
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

