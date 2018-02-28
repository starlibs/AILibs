
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.BayesianGaussianMixture;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BayesianGaussianMixture_reg_covar extends NumericRangeOptionPredicate {
	
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
		return false;
	}
}

