
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.MLPClassifier;
/*
    beta_1 : float, optional, default 0.9
    Exponential decay rate for estimates of first moment vector in adam,
    should be in [0, 1). Only used when solver='adam'


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_MLPClassifier_beta_1 extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.00001;
  }

  @Override
  protected double getMax() {
    return 0.99999;
  }

  @Override
  protected int getSteps() {
    return 10;
  }

  @Override
  protected boolean needsIntegers() {
    return false;
  }
}
