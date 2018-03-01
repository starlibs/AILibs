
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.DecisionTreeClassifier;
/*
    presort : bool, optional (default=False)
        Whether to presort the data to speed up the finding of best splits in
        fitting. For the default settings of a decision tree on large
        datasets, setting this to true may slow down the training process.
        When using either a smaller dataset or a restricted depth, this may
        speed up the training.

    Attributes
    
 */

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_DecisionTreeClassifier_presort extends NumericRangeOptionPredicate {
	
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

