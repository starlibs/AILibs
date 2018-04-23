package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.FactorAnalysis;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        svd_method : {"lapack", "randomized"}
        Which SVD method to use. If "lapack" use standard SVD from
        scipy.linalg, if "randomized" use fast ``randomized_svd`` function.
        Defaults to "randomized". For most applications "randomized" will
        be sufficiently precise while providing significant speed gains.
        Accuracy can also be improved by setting higher values for
        `iterated_power`. If this is not sufficient, for maximum precision
        you should choose "lapack".


    */
    public class OptionsFor_FactorAnalysis_svd_method extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"lapack", "randomized"});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
