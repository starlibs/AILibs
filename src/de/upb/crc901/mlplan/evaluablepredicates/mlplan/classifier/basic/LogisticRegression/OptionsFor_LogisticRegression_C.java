
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LogisticRegression;
/*
    C : float, default: 1.0
    Inverse of regularization strength; must be a positive float.
    Like in support vector machines, smaller values specify stronger
    regularization.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LogisticRegression_C extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0.01;
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
    return false;
  }

  @Override
  protected boolean isLinear() {
    return false;
  }
}
