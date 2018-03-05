
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LogisticRegressionCV;
/*
    intercept_scaling : float, default 1.
    Useful only when the solver 'liblinear' is used
    and self.fit_intercept is set to True. In this case, x becomes
    [x, self.intercept_scaling],
    i.e. a "synthetic" feature with constant value equal to
    intercept_scaling is appended to the instance vector.
    The intercept becomes ``intercept_scaling * synthetic_feature_weight``.

    Note! the synthetic feature weight is subject to l1/l2 regularization
    as all other features.
    To lessen the effect of regularization on synthetic feature weight
    (and therefore on the intercept) intercept_scaling has to be increased.


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.DisabledOptionPredicate;

// disabled due to dependence on solver
public class OptionsFor_LogisticRegressionCV_intercept_scaling extends DisabledOptionPredicate {

}
