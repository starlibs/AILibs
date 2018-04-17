package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.LinearSVC;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    dual : bool, (default=True)
    Select the algorithm to either solve the dual or primal
    optimization problem. Prefer dual=False when n_samples > n_features.


*/
public class OptionsFor_LinearSVC_dual extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "false" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
