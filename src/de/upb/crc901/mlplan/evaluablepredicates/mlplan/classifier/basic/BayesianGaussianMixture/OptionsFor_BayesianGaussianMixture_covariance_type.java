package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    covariance_type : {'full', 'tied', 'diag', 'spherical'}, defaults to 'full'
    String describing the type of covariance parameters to use.
    Must be one of::

        'full' (each component has its own general covariance matrix),
        'tied' (all components share the same general covariance matrix),
        'diag' (each component has its own diagonal covariance matrix),
        'spherical' (each component has its own single variance).


*/
public class OptionsFor_BayesianGaussianMixture_covariance_type extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "tied", "diag", "spherical" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
