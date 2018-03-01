package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        fit_intercept : bool
        Whether the intercept should be estimated or not. If False, the
        data is assumed to be already centered. Defaults to True.


    */
    public class OptionsFor_SGDClassifier_fit_intercept extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
