package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.MLPClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    early_stopping : bool, default False
    Whether to use early stopping to terminate training when validation
    score is not improving. If set to true, it will automatically set
    aside 10% of training data as validation and terminate training when
    validation score is not improving by at least tol for two consecutive
    epochs.
    Only effective when solver='sgd' or 'adam'


*/
public class OptionsFor_MLPClassifier_early_stopping extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
