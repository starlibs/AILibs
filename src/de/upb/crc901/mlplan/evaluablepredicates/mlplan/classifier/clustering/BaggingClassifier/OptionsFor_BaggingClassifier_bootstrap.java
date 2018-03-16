package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.BaggingClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        bootstrap : boolean, optional (default=True)
        Whether samples are drawn with replacement.


    */
    public class OptionsFor_BaggingClassifier_bootstrap extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
