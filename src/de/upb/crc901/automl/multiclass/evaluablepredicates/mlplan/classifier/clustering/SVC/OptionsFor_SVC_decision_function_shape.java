package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.SVC;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        decision_function_shape : 'ovo', 'ovr', default='ovr'
        Whether to return a one-vs-rest ('ovr') decision function of shape
        (n_samples, n_classes) as all other classifiers, or the original
        one-vs-one ('ovo') decision function of libsvm which has shape
        (n_samples, n_classes * (n_classes - 1) / 2).

        .. versionchanged:: 0.19
            decision_function_shape is 'ovr' by default.

        .. versionadded:: 0.17
           *decision_function_shape='ovr'* is recommended.

        .. versionchanged:: 0.17
           Deprecated *decision_function_shape='ovo' and None*.


    */
    public class OptionsFor_SVC_decision_function_shape extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
