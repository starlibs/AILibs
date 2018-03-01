
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.SVC;
/*
    C : float, optional (default=1.0)
        Penalty parameter C of the error term.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_SVC_C extends NumericRangeOptionPredicate {
	
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

