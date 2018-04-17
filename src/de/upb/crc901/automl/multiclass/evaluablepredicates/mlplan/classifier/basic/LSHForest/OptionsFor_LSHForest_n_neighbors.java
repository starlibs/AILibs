
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.LSHForest;
/*
    n_neighbors : int (default = 5)
    Number of neighbors to be returned from query function when
    it is not provided to the :meth:`kneighbors` method.


*/

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

public class OptionsFor_LSHForest_n_neighbors extends OptionsPredicate {
  private static List<Object> validValues = Arrays.asList(new Object[] { "1", "3", "11", "21", "101" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
