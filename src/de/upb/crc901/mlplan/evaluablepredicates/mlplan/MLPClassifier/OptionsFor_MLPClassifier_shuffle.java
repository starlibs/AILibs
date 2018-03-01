
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.MLPClassifier;
/*
    shuffle : bool, optional, default True
        Whether to shuffle samples in each iteration. Only used when
        solver='sgd' or 'adam'.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_MLPClassifier_shuffle extends NumericRangeOptionPredicate {
	
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

