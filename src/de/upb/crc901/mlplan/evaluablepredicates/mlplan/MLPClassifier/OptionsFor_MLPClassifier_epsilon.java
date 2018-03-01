
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.MLPClassifier;
/*
    epsilon : float, optional, default 1e-8
        Value for numerical stability in adam. Only used when solver='adam'

    Attributes
    
 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_MLPClassifier_epsilon extends NumericRangeOptionPredicate {
	
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

