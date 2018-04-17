package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.MLPClassifier;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        shuffle : bool, optional, default True
        Whether to shuffle samples in each iteration. Only used when
        solver='sgd' or 'adam'.


    */
    public class OptionsFor_MLPClassifier_shuffle extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
