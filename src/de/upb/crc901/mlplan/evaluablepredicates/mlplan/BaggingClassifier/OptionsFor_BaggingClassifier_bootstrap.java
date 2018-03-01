
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.BaggingClassifier;
/*
    bootstrap : boolean, optional (default=True)
        Whether samples are drawn with replacement.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BaggingClassifier_bootstrap extends NumericRangeOptionPredicate {
	
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

