
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.ExtraTreeClassifier;
/*
    min_weight_fraction_leaf : float, optional (default=0.)
    The minimum weighted fraction of the sum total of weights (of all
    the input samples) required to be at a leaf node. Samples have
    equal weight when sample_weight is not provided.


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_ExtraTreeClassifier_min_weight_fraction_leaf extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.0001;
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
