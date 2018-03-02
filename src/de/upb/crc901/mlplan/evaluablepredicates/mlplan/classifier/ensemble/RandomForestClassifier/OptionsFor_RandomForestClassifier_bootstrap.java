package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.ensemble.RandomForestClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        bootstrap : boolean, optional (default=True)
        Whether bootstrap samples are used when building trees.


    */
    public class OptionsFor_RandomForestClassifier_bootstrap extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
