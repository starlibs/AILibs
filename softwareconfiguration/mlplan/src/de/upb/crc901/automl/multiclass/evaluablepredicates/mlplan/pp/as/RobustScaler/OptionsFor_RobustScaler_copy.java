package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.RobustScaler;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;

/*
    copy : boolean, optional, default is True
    If False, try to avoid a copy and do inplace scaling instead.
    This is not guaranteed to always work inplace; e.g. if the data is
    not a NumPy array or scipy.sparse CSR matrix, a copy may still be
    returned.

Attributes

*/
public class OptionsFor_RobustScaler_copy extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "false" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
