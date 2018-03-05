package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.GaussianProcessClassifier;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    warm_start : bool, optional (default: False)
    If warm-starts are enabled, the solution of the last Newton iteration
    on the Laplace approximation of the posterior mode is used as
    initialization for the next call of _posterior_mode(). This can speed
    up convergence when _posterior_mode is called several times on similar
    problems as in hyperparameter optimization.


*/
public class OptionsFor_GaussianProcessClassifier_warm_start extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
