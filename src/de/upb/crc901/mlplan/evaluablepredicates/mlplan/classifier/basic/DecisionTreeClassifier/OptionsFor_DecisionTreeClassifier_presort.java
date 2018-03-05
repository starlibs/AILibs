package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.DecisionTreeClassifier;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    presort : bool, optional (default=False)
    Whether to presort the data to speed up the finding of best splits in
    fitting. For the default settings of a decision tree on large
    datasets, setting this to true may slow down the training process.
    When using either a smaller dataset or a restricted depth, this may
    speed up the training.

Attributes

*/
public class OptionsFor_DecisionTreeClassifier_presort extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
