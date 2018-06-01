package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.CCA;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        copy : boolean
        Whether the deflation be done on a copy. Let the default value
        to True unless you don't care about side effects

    Attributes
    
    */
    public class OptionsFor_CCA_copy extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
