
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.SVC;
/*
    shrinking : boolean, optional (default=True)
        Whether to use the shrinking heuristic.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_SVC_shrinking extends NumericRangeOptionPredicate {
	
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

