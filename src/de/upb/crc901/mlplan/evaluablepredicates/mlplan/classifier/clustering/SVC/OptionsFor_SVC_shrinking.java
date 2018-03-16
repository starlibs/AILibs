package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SVC;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        shrinking : boolean, optional (default=True)
        Whether to use the shrinking heuristic.


    */
    public class OptionsFor_SVC_shrinking extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
