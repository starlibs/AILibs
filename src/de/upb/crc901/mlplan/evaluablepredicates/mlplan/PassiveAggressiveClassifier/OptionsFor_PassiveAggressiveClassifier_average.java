
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.PassiveAggressiveClassifier;
/*
    average : bool or int, optional
        When set to True, computes the averaged SGD weights and stores the
        result in the ``coef_`` attribute. If set to an int greater than 1,
        averaging will begin once the total number of samples seen reaches
        average. So average=10 will begin averaging after seeing 10 samples.

        .. versionadded:: 0.19
           parameter *average* to use weights averaging in SGD


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_PassiveAggressiveClassifier_average extends NumericRangeOptionPredicate {
	
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

