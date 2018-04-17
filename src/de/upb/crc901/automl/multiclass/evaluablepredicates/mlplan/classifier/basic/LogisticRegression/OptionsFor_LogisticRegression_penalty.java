package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.LogisticRegression;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    penalty : str, 'l1' or 'l2', default: 'l2'
    Used to specify the norm used in the penalization. The 'newton-cg',
    'sag' and 'lbfgs' solvers support only l2 penalties.

    .. versionadded:: 0.19
       l1 penalty with SAGA solver (allowing 'multinomial' + L1)


*/
public class OptionsFor_LogisticRegression_penalty extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "l1" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
