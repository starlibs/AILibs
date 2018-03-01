
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LogisticRegression;
/*
    verbose : int, default: 0
        For the liblinear and lbfgs solvers set verbose to any positive
        number for verbosity.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LogisticRegression_verbose extends NumericRangeOptionPredicate {
	
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

