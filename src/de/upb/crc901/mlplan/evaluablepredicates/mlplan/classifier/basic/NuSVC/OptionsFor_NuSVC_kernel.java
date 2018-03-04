package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.NuSVC;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        kernel : string, optional (default='rbf')
         Specifies the kernel type to be used in the algorithm.
         It must be one of 'linear', 'poly', 'rbf', 'sigmoid', 'precomputed' or
         a callable.
         If none is given, 'rbf' will be used. If a callable is given it is
         used to precompute the kernel matrix.


    */
    public class OptionsFor_NuSVC_kernel extends OptionsPredicate {
        
        private static List<String> validValues = Arrays.asList(new String[]{"linear", "poly", "sigmoid", "precomputed"}); // rbf is default

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
