package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LogisticRegressionCV;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    refit : bool
    If set to True, the scores are averaged across all folds, and the
    coefs and the C that corresponds to the best score is taken, and a
    final refit is done using these parameters.
    Otherwise the coefs, intercepts and C that correspond to the
    best scores across folds are averaged.


*/
public class OptionsFor_LogisticRegressionCV_refit extends OptionsPredicate {

  // FIXME remove default parameter from this list
  private static List<Object> validValues = Arrays.asList(new Object[] { "true", "false" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
