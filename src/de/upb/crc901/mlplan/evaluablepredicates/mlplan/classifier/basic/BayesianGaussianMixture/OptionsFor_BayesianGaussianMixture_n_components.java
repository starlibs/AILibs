
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;
/*
    n_components : int, defaults to 1.
    The number of mixture components. Depending on the data and the value
    of the `weight_concentration_prior` the model can decide to not use
    all the components by setting some component `weights_` to values very
    close to zero. The number of effective components is therefore smaller
    than n_components.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BayesianGaussianMixture_n_components extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 2;
  }

  // XXX FIXME max value unclear. Theoretically, a value of Integer.MAX_VALUE would be possible.
  @Override
  protected double getMax() {
    return 10;
  }

  @Override
  protected int getSteps() {
    return 10;
  }

  @Override
  protected boolean needsIntegers() {
    return true;
  }
}
