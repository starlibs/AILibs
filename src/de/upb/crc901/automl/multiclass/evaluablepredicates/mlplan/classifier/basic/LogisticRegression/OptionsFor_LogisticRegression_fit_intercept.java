package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.LogisticRegression;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    fit_intercept : bool, default: True
    Specifies if a constant (a.k.a. bias or intercept) should be
    added to the decision function.


*/
public class OptionsFor_LogisticRegression_fit_intercept extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
