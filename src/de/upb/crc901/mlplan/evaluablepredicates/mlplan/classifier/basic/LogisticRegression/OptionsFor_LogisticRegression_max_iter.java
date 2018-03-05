
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LogisticRegression;
/*
    max_iter : int, default: 100
    Useful only for the newton-cg, sag and lbfgs solvers.
    Maximum number of iterations taken for the solvers to converge.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LogisticRegression_max_iter extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 1;
  }

  @Override
  protected double getMax() {
    return 10000;
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
