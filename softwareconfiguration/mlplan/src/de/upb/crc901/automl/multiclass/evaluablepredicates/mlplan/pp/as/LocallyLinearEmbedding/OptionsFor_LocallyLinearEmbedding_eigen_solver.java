package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.LocallyLinearEmbedding;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        eigen_solver : string, {'auto', 'arpack', 'dense'}
        auto : algorithm will attempt to choose the best method for input data

        arpack : use arnoldi iteration in shift-invert mode.
                    For this method, M may be a dense matrix, sparse matrix,
                    or general linear operator.
                    Warning: ARPACK can be unstable for some problems.  It is
                    best to try several random seeds in order to check results.

        dense  : use standard dense matrix operations for the eigenvalue
                    decomposition.  For this method, M must be an array
                    or matrix type.  This method should be avoided for
                    large problems.


    */
    public class OptionsFor_LocallyLinearEmbedding_eigen_solver extends OptionsPredicate {
        
        private static List<Object> validValues = Arrays.asList(new Object[]{});

        @Override
        protected List<? extends Object> getValidValues() {
            return validValues;
        }
    }
    
