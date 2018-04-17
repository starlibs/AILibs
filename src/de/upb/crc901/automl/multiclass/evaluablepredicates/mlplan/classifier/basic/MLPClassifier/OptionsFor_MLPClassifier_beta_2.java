
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.MLPClassifier;
/*
    beta_2 : float, optional, default 0.999
    Exponential decay rate for estimates of second moment vector in adam,
    should be in [0, 1). Only used when solver='adam'


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_MLPClassifier_beta_2 extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.5;
  }

  @Override
  protected double getMax() {
    return 0.998;
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
