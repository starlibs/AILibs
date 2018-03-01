
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.RandomForestClassifier;
/*
    oob_score : bool (default=False)
        Whether to use out-of-bag samples to estimate
        the generalization accuracy.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_RandomForestClassifier_oob_score extends NumericRangeOptionPredicate {
	
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

