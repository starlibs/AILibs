package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.RobustScaler;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        with_scaling : boolean, True by default
        If True, scale the data to interquartile range.


    */
    public class OptionsFor_RobustScaler_with_scaling extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
