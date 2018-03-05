
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;
/*
    verbose_interval : int, default to 10.
    Number of iteration done before the next print.

Attributes

*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

// Ignore this option as it only controls the output
public class OptionsFor_BayesianGaussianMixture_verbose_interval extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 1;
  }

  @Override
  protected double getMax() {
    return 1;
  }

  @Override
  protected int getSteps() {
    return -1;
  }

  @Override
  protected boolean needsIntegers() {
    return true;
  }
}
