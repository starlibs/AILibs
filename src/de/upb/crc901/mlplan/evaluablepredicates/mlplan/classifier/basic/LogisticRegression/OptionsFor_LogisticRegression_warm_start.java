package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LogisticRegression;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    warm_start : bool, default: False
    When set to True, reuse the solution of the previous call to fit as
    initialization, otherwise, just erase the previous solution.
    Useless for liblinear solver.

    .. versionadded:: 0.17
       *warm_start* to support *lbfgs*, *newton-cg*, *sag*, *saga* solvers.


*/
public class OptionsFor_LogisticRegression_warm_start extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
