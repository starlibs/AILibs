
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.BernoulliNB;
/*
    binarize : float or None, optional (default=0.0)
    Threshold for binarizing (mapping to booleans) of sample features.
    If None, input is presumed to already consist of binary vectors.


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BernoulliNB_binarize extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.00001;
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
