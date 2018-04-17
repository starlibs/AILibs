
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.GaussianProcessClassifier;
/*
    max_iter_predict : int, optional (default: 100)
    The maximum number of iterations in Newton's method for approximating
    the posterior during predict. Smaller values will reduce computation
    time at the cost of worse results.


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_GaussianProcessClassifier_max_iter_predict extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 1;
  }

  @Override
  protected double getMax() {
    return 1000;
  }

  @Override
  protected int getSteps() {
    return 10;
  }

  @Override
  protected boolean needsIntegers() {
    return true;
  }

  @Override
  protected boolean isLinear() {
    return false;
  }
}
