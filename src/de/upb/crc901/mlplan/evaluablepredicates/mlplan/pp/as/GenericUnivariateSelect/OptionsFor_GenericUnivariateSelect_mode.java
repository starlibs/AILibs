package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.GenericUnivariateSelect;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        mode : {'percentile', 'k_best', 'fpr', 'fdr', 'fwe'}
        Feature selection mode.


    */
    public class OptionsFor_GenericUnivariateSelect_mode extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
