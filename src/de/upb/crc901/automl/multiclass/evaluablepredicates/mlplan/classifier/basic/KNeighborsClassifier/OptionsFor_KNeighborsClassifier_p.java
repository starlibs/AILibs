
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.KNeighborsClassifier;
/*
    p : integer, optional (default = 2)
    Power parameter for the Minkowski metric. When p = 1, this is
    equivalent to using manhattan_distance (l1), and euclidean_distance
    (l2) for p = 2. For arbitrary p, minkowski_distance (l_p) is used.


*/

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

public class OptionsFor_KNeighborsClassifier_p extends OptionsPredicate {

  private static List<Integer> validValues = Arrays.asList(new Integer[] { 1, 2, 1000 });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
