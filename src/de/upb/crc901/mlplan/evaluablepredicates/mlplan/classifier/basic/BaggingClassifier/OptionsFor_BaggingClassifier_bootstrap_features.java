package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BaggingClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        bootstrap_features : boolean, optional (default=False)
        Whether features are drawn with replacement.


    */
    public class OptionsFor_BaggingClassifier_bootstrap_features extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
