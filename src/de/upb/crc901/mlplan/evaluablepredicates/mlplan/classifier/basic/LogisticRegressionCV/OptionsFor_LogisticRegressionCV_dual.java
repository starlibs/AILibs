package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LogisticRegressionCV;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.DisabledOptionPredicate;

import java.util.Arrays;
import java.util.List;

/*
    dual : bool
    Dual or primal formulation. Dual formulation is only implemented for
    l2 penalty with liblinear solver. Prefer dual=False when
    n_samples > n_features.


*/
// disabled due to dependence on solver, penalty and number of instances, features
public class OptionsFor_LogisticRegressionCV_dual extends DisabledOptionPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true", "false" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
