package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.QuantileTransformer;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        copy : boolean, optional, (default=True)
        Set to False to perform inplace transformation and avoid a copy (if the
        input is already a numpy array).

    Attributes
    
    */
    public class OptionsFor_QuantileTransformer_copy extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
