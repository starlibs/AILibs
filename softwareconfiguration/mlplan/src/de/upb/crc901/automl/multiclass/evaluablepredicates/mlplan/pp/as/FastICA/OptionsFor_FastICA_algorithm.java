package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.FastICA;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        algorithm : {"parallel", "deflation"}
        Apply parallel or deflational algorithm for FastICA.


    */
    public class OptionsFor_FastICA_algorithm extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"parallel", "deflation"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
