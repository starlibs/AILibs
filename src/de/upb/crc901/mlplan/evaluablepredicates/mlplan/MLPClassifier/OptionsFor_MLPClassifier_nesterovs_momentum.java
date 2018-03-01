
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.MLPClassifier;
/*
    nesterovs_momentum : boolean, default True
        Whether to use Nesterov's momentum. Only used when solver='sgd' and
        momentum > 0.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_MLPClassifier_nesterovs_momentum extends NumericRangeOptionPredicate {
	
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

