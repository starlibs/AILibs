
package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.ensemble.RandomForestClassifier;
/*
    min_impurity_decrease : float, optional (default=0.)
    A node will be split if this split induces a decrease of the impurity
    greater than or equal to this value.

    The weighted impurity decrease equation is the following::

        N_t / N * (impurity - N_t_R / N_t * right_impurity
                            - N_t_L / N_t * left_impurity)

    where ``N`` is the total number of samples, ``N_t`` is the number of
    samples at the current node, ``N_t_L`` is the number of samples in the
    left child, and ``N_t_R`` is the number of samples in the right child.

    ``N``, ``N_t``, ``N_t_R`` and ``N_t_L`` all refer to the weighted sum,
    if ``sample_weight`` is passed.

    .. versionadded:: 0.19


*/

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.ensemble.ExtraTreesClassifier.OptionsFor_ExtraTreesClassifier_min_impurity_decrease;

public class OptionsFor_RandomForestClassifier_min_impurity_decrease extends OptionsFor_ExtraTreesClassifier_min_impurity_decrease {

}
