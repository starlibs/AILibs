package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.RandomForestClassifier;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        oob_score : bool (default=False)
        Whether to use out-of-bag samples to estimate
        the generalization accuracy.


    */
    public class OptionsFor_RandomForestClassifier_oob_score extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
