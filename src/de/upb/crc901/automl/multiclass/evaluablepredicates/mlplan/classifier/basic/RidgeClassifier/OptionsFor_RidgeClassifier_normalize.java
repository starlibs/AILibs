package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.RidgeClassifier;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        normalize : boolean, optional, default False
        This parameter is ignored when ``fit_intercept`` is set to False.
        If True, the regressors X will be normalized before regression by
        subtracting the mean and dividing by the l2-norm.
        If you wish to standardize, please use
        :class:`sklearn.preprocessing.StandardScaler` before calling ``fit``
        on an estimator with ``normalize=False``.


    */
    public class OptionsFor_RidgeClassifier_normalize extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true" }); // default is false

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
