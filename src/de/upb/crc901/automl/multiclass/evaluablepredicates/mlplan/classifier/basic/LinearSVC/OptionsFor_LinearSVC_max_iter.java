
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.LinearSVC;
/*
    max_iter : int, (default=1000)
    The maximum number of iterations to be run.

Attributes

*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LinearSVC_max_iter extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 10;
  }

  @Override
  protected double getMax() {
    return 1000000;
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
