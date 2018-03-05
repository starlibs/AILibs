
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LSHForest;
/*
    n_estimators : int (default = 10)
    Number of trees in the LSH Forest.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

public class OptionsFor_LSHForest_n_estimators extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "3", "5", "20", "100", "200", "500", "1000" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
