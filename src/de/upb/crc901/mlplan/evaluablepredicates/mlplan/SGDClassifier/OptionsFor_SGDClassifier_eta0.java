
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.SGDClassifier;
/*
    eta0 : double
        The initial learning rate for the 'constant' or 'invscaling'
        schedules. The default value is 0.0 as eta0 is not used by the
        default schedule 'optimal'.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_SGDClassifier_eta0 extends NumericRangeOptionPredicate {
	
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

