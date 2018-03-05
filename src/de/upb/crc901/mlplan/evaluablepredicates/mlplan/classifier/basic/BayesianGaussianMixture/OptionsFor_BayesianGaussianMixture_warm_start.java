package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    warm_start : bool, default to False.
    If 'warm_start' is True, the solution of the last fitting is used as
    initialization for the next call of fit(). This can speed up
    convergence when fit is called several time on similar problems.


*/
public class OptionsFor_BayesianGaussianMixture_warm_start extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
