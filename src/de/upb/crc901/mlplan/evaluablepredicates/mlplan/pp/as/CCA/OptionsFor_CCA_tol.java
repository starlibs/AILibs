
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.CCA;
    /*
        tol : non-negative real, default 1e-06.
        the tolerance used in the iterative algorithm


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_CCA_tol extends NumericRangeOptionPredicate {
        
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
    
