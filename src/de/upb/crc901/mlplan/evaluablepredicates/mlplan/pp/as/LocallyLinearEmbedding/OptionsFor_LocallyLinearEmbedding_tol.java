
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.LocallyLinearEmbedding;
    /*
        tol : float, optional
        Tolerance for 'arpack' method
        Not used if eigen_solver=='dense'.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LocallyLinearEmbedding_tol extends NumericRangeOptionPredicate {
        
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
    
