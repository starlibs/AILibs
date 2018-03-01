
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.NuSVC;
/*
    max_iter : int, optional (default=-1)
        Hard limit on iterations within solver, or -1 for no limit.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_NuSVC_max_iter extends NumericRangeOptionPredicate {
	
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

