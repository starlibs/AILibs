
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.LogisticRegression;
/*
    tol : float, default: 1e-4
    Tolerance for stopping criteria.


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LogisticRegression_tol extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.00001;
  }

  @Override
  protected double getMax() {
    return 0.1;
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
