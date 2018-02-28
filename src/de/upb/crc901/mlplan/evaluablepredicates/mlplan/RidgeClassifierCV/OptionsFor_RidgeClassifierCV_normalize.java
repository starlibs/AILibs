
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.RidgeClassifierCV;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_RidgeClassifierCV_normalize extends NumericRangeOptionPredicate {
	
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

