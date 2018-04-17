package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.KNeighborsClassifier;

import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.DisabledOptionPredicate;

/*
    metric : string or callable, default 'minkowski'
    the distance metric to use for the tree.  The default metric is
    minkowski, and with p=2 is equivalent to the standard Euclidean
    metric. See the documentation of the DistanceMetric class for a
    list of available metrics.


*/
// XXX FIXME do not know other metrics
public class OptionsFor_KNeighborsClassifier_metric extends DisabledOptionPredicate {

  private static List<Object> validValues = Arrays.asList(new Object[] {});

  @Override
  protected List<? extends Object> getValidValues() {
    return validValues;
  }
}
