package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.Normalizer;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        norm : "l1", "l2", or "max", optional ("l2" by default)
        The norm to use to normalize each non zero sample.


    */
    public class OptionsFor_Normalizer_norm extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"l1", "max"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
