package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.MaxAbsScaler;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;

import java.util.Arrays;
import java.util.List;

/*
    copy : boolean, optional, default is True
    Set to False to perform inplace scaling and avoid a copy (if the input
    is already a numpy array).

Attributes

*/
public class OptionsFor_MaxAbsScaler_copy extends OptionsPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] { "false" });

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
