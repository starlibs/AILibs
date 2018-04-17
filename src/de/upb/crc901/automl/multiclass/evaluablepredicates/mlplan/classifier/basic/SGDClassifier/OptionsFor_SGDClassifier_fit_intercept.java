package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;

    import java.util.Arrays;
import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.RidgeClassifier.OptionsFor_RidgeClassifier_fit_intercept;
    /*
        fit_intercept : bool
        Whether the intercept should be estimated or not. If False, the
        data is assumed to be already centered. Defaults to True.


    */
    public class OptionsFor_SGDClassifier_fit_intercept extends OptionsFor_RidgeClassifier_fit_intercept {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"false"}); // default is true

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
