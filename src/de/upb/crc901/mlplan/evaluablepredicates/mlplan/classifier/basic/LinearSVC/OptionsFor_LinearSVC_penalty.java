package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LinearSVC;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    penalty : string, 'l1' or 'l2' (default='l2')
    Specifies the norm used in the penalization. The 'l2'
    penalty is the standard used in SVC. The 'l1' leads to ``coef_``
    vectors that are sparse.


*/
public class OptionsFor_LinearSVC_penalty extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "l1" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
