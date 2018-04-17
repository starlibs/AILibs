package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        shuffle : bool, optional
        Whether or not the training data should be shuffled after each epoch.
        Defaults to True.


    */
    public class OptionsFor_SGDClassifier_shuffle extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"false"}); // true is default

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
