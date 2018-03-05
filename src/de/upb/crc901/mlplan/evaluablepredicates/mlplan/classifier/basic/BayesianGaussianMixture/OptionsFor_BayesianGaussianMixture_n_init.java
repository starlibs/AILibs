
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;
/*
    n_init : int, defaults to 1.
    The number of initializations to perform. The result with the highest
    lower bound value on the likelihood is kept.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BayesianGaussianMixture_n_init extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 2;
  }

  @Override
  protected double getMax() {
    return 10;
  }

  @Override
  protected int getSteps() {
    return 1;
  }

  @Override
  protected boolean needsIntegers() {
    return true;
  }
}
