package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.MLPClassifier;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        verbose : bool, optional, default False
        Whether to print progress messages to stdout.


    */
    public class OptionsFor_MLPClassifier_verbose extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
