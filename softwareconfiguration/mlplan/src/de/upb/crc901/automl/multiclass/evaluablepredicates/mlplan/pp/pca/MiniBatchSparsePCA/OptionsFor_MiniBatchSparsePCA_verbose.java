package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.MiniBatchSparsePCA;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        verbose : int
        Controls the verbosity; the higher, the more messages. Defaults to 0.


    */
    public class OptionsFor_MiniBatchSparsePCA_verbose extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"true", "false"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
