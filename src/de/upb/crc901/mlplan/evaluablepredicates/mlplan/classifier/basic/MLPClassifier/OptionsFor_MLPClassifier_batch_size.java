package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.MLPClassifier;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

/*
    batch_size : int, optional, default 'auto'
    Size of minibatches for stochastic optimizers.
    If the solver is 'lbfgs', the classifier will not use minibatch.
    When set to "auto", `batch_size=min(200, n_samples)`


*/
public class OptionsFor_MLPClassifier_batch_size extends NumericRangeOptionPredicate {

  @Override
  protected double getMin() {
    // TODO Auto-generated method stub
    return 100;
  }

  @Override
  protected double getMax() {
    // TODO Auto-generated method stub
    return 1000;
  }

  @Override
  protected int getSteps() {
    // TODO Auto-generated method stub
    return 10;
  }

  @Override
  protected boolean needsIntegers() {
    return true;
  }
}
