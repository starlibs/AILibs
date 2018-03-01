package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.LinearDiscriminantAnalysis;

    import java.util.Arrays;
    import java.util.List;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        solver : string, optional
        Solver to use, possible values:
          - 'svd': Singular value decomposition (default).
            Does not compute the covariance matrix, therefore this solver is
            recommended for data with a large number of features.
          - 'lsqr': Least squares solution, can be combined with shrinkage.
          - 'eigen': Eigenvalue decomposition, can be combined with shrinkage.


    */
    public class OptionsFor_LinearDiscriminantAnalysis_solver extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
