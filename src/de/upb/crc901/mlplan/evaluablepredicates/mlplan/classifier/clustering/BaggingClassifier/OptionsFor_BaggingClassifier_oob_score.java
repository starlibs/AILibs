package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.BaggingClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        oob_score : bool
        Whether to use out-of-bag samples to estimate
        the generalization error.


    */
    public class OptionsFor_BaggingClassifier_oob_score extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
