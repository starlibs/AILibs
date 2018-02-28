
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.LogisticRegression;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LogisticRegression_intercept_scaling extends NumericRangeOptionPredicate {
	
	@Override
	protected double getMin() {
		return 0;
	}

	@Override
	protected double getMax() {
		return 3;
	}

	@Override
	protected int getSteps() {
		return 3;
	}

	@Override
	protected boolean needsIntegers() {
		return true;
	}
}

