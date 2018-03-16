
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.pca.PCA;
    /*
        tol : float >= 0, optional (default .0)
        Tolerance for singular values computed by svd_solver == 'arpack'.

        .. versionadded:: 0.18.0


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_PCA_tol extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return -1;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
