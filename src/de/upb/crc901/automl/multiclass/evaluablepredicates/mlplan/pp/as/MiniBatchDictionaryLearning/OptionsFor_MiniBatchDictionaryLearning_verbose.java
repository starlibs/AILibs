package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.MiniBatchDictionaryLearning;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        verbose : bool, optional (default: False)
        To control the verbosity of the procedure.


    */
    public class OptionsFor_MiniBatchDictionaryLearning_verbose extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
