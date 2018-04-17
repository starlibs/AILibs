
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.LinearSVC;
/*
    C : float, optional (default=1.0)
    Penalty parameter C of the error term.


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LinearSVC_C extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.01;
  }

  @Override
  protected double getMax() {
    return 100000;
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
