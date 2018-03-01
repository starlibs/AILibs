
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LogisticRegressionCV;
/*
    n_jobs : int, optional
        Number of CPU cores used during the cross-validation loop. If given
        a value of -1, all cores are used.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LogisticRegressionCV_n_jobs extends NumericRangeOptionPredicate {
	
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

