package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.ensemble.RandomForestClassifier;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        bootstrap : boolean, optional (default=True)
        Whether bootstrap samples are used when building trees.


    */
    public class OptionsFor_RandomForestClassifier_bootstrap extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"false"}); // default is true

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
