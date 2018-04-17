package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.BayesianGaussianMixture;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    weight_concentration_prior_type : str, defaults to 'dirichlet_process'.
    String describing the type of the weight concentration prior.
    Must be one of::

        'dirichlet_process' (using the Stick-breaking representation),
        'dirichlet_distribution' (can favor more uniform weights).

weight_concentration_prior : float | None, optional.
    The dirichlet concentration of each component on the weight
    distribution (Dirichlet). This is commonly called gamma in the
    literature. The higher concentration puts more mass in
    the center and will lead to more components being active, while a lower
    concentration parameter will lead to more mass at the edge of the
    mixture weights simplex. The value of the parameter must be greater
    than 0. If it is None, it's set to ``1. / n_components``.


*/
public class OptionsFor_BayesianGaussianMixture_weight_concentration_prior_type extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "dirichlet_distribution" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
