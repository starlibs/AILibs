package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.ExtraTreeClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    splitter : string, optional (default="best")
    The strategy used to choose the split at each node. Supported
    strategies are "best" to choose the best split and "random" to choose
    the best random split.


*/
public class OptionsFor_ExtraTreeClassifier_splitter extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "random" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
