package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.RandomizedLogisticRegression;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        verbose : boolean or integer, optional
        Sets the verbosity amount


    */
    public class OptionsFor_RandomizedLogisticRegression_verbose extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
