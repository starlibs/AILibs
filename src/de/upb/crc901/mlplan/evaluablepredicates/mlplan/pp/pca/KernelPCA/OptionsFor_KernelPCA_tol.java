
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.pca.KernelPCA;
    /*
        tol : float, default=0
        Convergence tolerance for arpack.
        If 0, optimal value will be chosen by arpack.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_KernelPCA_tol extends NumericRangeOptionPredicate {
        
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
            return true;
        }
    }
    
