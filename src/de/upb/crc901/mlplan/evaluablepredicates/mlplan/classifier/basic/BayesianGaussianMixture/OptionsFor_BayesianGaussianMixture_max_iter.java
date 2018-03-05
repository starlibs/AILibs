
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;
/*
    max_iter : int, defaults to 100.
    The number of EM iterations to perform.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_BayesianGaussianMixture_max_iter extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 1;
  }

  // XXX FIXME here it is not clear what the upper bound should look like.
  // Theoretically, it could be Integer.MAX_VALUE, but might make
  //
  @Override
  protected double getMax() {
    return 1000;
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
