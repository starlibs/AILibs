
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.BaggingClassifier;
/*
    bootstrap_features : boolean, optional (default=False)
        Whether features are drawn with replacement.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BaggingClassifier_bootstrap_features extends NumericRangeOptionPredicate {
	
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

