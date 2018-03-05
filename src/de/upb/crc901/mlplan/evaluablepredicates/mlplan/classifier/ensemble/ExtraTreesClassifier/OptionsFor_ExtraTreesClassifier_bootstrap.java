package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.ensemble.ExtraTreesClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        bootstrap : boolean, optional (default=False)
        Whether bootstrap samples are used when building trees.


    */
    public class OptionsFor_ExtraTreesClassifier_bootstrap extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true"}); // false is default

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
