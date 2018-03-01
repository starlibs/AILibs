
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.SGDClassifier;
/*
    shuffle : bool, optional
        Whether or not the training data should be shuffled after each epoch.
        Defaults to True.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_SGDClassifier_shuffle extends NumericRangeOptionPredicate {
	
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

