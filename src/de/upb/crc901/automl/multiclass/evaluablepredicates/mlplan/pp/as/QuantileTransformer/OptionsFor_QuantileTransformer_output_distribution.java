package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.QuantileTransformer;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        output_distribution : str, optional (default='uniform')
        Marginal distribution for the transformed data. The choices are
        'uniform' (default) or 'normal'.


    */
    public class OptionsFor_QuantileTransformer_output_distribution extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
