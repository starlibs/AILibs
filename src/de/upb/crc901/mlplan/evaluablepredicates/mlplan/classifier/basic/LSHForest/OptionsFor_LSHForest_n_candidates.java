
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LSHForest;
/*
    n_candidates : int (default = 50)
    Minimum number of candidates evaluated per estimator, assuming enough
    items meet the `min_hash_match` constraint.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

public class OptionsFor_LSHForest_n_candidates extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    return 10;
  }

  @Override
  protected double getMax() {
    return 200;
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
