package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.RidgeClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        copy_X : boolean, optional, default True
        If True, X will be copied; else, it may be overwritten.


    */
    public class OptionsFor_RidgeClassifier_copy_X extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
