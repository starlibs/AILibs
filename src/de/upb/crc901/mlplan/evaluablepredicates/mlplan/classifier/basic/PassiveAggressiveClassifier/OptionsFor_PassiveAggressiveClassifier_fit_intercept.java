package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.PassiveAggressiveClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        fit_intercept : bool, default=False
        Whether the intercept should be estimated or not. If False, the
        data is assumed to be already centered.


    */
    public class OptionsFor_PassiveAggressiveClassifier_fit_intercept extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
