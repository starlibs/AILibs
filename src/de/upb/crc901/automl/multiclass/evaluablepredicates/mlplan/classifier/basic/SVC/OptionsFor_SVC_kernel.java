package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.SVC;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        kernel : string, optional (default='rbf')
         Specifies the kernel type to be used in the algorithm.
         It must be one of 'linear', 'poly', 'rbf', 'sigmoid', 'precomputed' or
         a callable.
         If none is given, 'rbf' will be used. If a callable is given it is
         used to pre-compute the kernel matrix from data matrices; that matrix
         should be an array of shape ``(n_samples, n_samples)``.


    */
    public class OptionsFor_SVC_kernel extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{"linear", "poly", "sigmoid", "precomputed"}); // rbf is default

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
