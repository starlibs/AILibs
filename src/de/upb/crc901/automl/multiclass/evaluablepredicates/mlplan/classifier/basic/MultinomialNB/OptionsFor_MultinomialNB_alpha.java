
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.MultinomialNB;
/*
    alpha : float, optional (default=1.0)
    Additive (Laplace/Lidstone) smoothing parameter
    (0 for no smoothing).


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_MultinomialNB_alpha extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 0;
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
}
