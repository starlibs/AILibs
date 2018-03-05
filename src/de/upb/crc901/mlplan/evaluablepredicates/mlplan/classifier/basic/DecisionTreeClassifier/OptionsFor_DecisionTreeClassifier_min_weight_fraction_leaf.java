
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.DecisionTreeClassifier;
/*
    min_weight_fraction_leaf : float, optional (default=0.)
    The minimum weighted fraction of the sum total of weights (of all
    the input samples) required to be at a leaf node. Samples have
    equal weight when sample_weight is not provided.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_DecisionTreeClassifier_min_weight_fraction_leaf extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.001;
  }

  @Override
  protected double getMax() {
    return 1;
  }

  @Override
  protected int getSteps() {
    return 10;
  }

  @Override
  protected boolean needsIntegers() {
    return false;
  }

  @Override
  protected boolean isLinear() {
    return false;
  }

}
