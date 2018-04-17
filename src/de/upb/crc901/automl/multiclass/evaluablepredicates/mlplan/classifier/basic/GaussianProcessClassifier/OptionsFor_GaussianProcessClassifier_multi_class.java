package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.GaussianProcessClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    multi_class : string, default : "one_vs_rest"
    Specifies how multi-class classification problems are handled.
    Supported are "one_vs_rest" and "one_vs_one". In "one_vs_rest",
    one binary Gaussian process classifier is fitted for each class, which
    is trained to separate this class from the rest. In "one_vs_one", one
    binary Gaussian process classifier is fitted for each pair of classes,
    which is trained to separate these two classes. The predictions of
    these binary predictors are combined into multi-class predictions.
    Note that "one_vs_one" does not support predicting probability
    estimates.


*/
public class OptionsFor_GaussianProcessClassifier_multi_class extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "one_vs_one" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
