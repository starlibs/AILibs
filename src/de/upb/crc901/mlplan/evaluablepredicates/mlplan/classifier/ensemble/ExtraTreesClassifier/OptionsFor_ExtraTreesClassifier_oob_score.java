package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.ensemble.ExtraTreesClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        oob_score : bool, optional (default=False)
        Whether to use out-of-bag samples to estimate
        the generalization accuracy.


    */
    public class OptionsFor_ExtraTreesClassifier_oob_score extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true"}); // default is false

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
