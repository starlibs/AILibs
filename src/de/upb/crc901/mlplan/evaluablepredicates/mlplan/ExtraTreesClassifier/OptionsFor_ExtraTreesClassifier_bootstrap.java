
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.ExtraTreesClassifier;
/*
    bootstrap : boolean, optional (default=False)
        Whether bootstrap samples are used when building trees.


 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_ExtraTreesClassifier_bootstrap extends NumericRangeOptionPredicate {
	
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

