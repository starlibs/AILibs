
package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.DecisionTreeClassifier;
/*
    min_samples_leaf : int, float, optional (default=1)
    The minimum number of samples required to be at a leaf node:

    - If int, then consider `min_samples_leaf` as the minimum number.
    - If float, then `min_samples_leaf` is a percentage and
      `ceil(min_samples_leaf * n_samples)` are the minimum
      number of samples for each node.

    .. versionchanged:: 0.18
       Added float values for percentages.


*/

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.DisabledOptionPredicate;

// XXX probably unfeasible to set it to a higher value.
public class OptionsFor_DecisionTreeClassifier_min_samples_leaf extends DisabledOptionPredicate {

}
