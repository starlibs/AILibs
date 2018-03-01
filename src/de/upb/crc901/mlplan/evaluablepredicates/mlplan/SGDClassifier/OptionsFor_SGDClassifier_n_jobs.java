
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.SGDClassifier;
/*
    n_jobs : integer, optional
        The number of CPUs to use to do the OVA (One Versus All, for
        multi-class problems) computation. -1 means 'all CPUs'. Defaults
        to 1.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_SGDClassifier_n_jobs extends NumericRangeOptionPredicate {
	
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

