package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.KNeighborsClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    algorithm : {'auto', 'ball_tree', 'kd_tree', 'brute'}, optional
    Algorithm used to compute the nearest neighbors:

    - 'ball_tree' will use :class:`BallTree`
    - 'kd_tree' will use :class:`KDTree`
    - 'brute' will use a brute-force search.
    - 'auto' will attempt to decide the most appropriate algorithm
      based on the values passed to :meth:`fit` method.

    Note: fitting on sparse input will override the setting of
    this parameter, using brute force.


*/
public class OptionsFor_KNeighborsClassifier_algorithm extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "auto", "ball_tree", "kd_tree", "brute" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
