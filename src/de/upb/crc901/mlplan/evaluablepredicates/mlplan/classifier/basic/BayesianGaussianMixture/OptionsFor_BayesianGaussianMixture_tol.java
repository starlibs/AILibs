
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;
/*
    tol : float, defaults to 1e-3.
    The convergence threshold. EM iterations will stop when the
    lower bound average gain on the likelihood (of the training data with
    respect to the model) is below this threshold.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BayesianGaussianMixture_tol extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return Math.pow(10, -10);
  }

  @Override
  protected double getMax() {
    return Math.pow(10, -4);
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
