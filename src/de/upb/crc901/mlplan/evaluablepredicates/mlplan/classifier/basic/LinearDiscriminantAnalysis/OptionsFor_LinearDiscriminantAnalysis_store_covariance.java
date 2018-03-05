package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LinearDiscriminantAnalysis;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    store_covariance : bool, optional
    Additionally compute class covariance matrix (default False), used
    only in 'svd' solver.

    .. versionadded:: 0.17


*/
public class OptionsFor_LinearDiscriminantAnalysis_store_covariance extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "true" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
