
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LogisticRegressionCV;
/*
    tol : float, optional
        Tolerance for stopping criteria.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LogisticRegressionCV_tol extends NumericRangeOptionPredicate {
	
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
		return false;
	}
}

