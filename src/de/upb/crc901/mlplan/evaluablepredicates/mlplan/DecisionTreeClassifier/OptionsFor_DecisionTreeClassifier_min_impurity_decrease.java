
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.DecisionTreeClassifier;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_DecisionTreeClassifier_min_impurity_decrease extends NumericRangeOptionPredicate {
	
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

