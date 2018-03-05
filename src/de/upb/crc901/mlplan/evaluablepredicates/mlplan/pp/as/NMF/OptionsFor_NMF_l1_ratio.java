
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.NMF;
    /*
        l1_ratio : double, default: 0.
        The regularization mixing parameter, with 0 <= l1_ratio <= 1.
        For l1_ratio = 0 the penalty is an elementwise L2 penalty
        (aka Frobenius Norm).
        For l1_ratio = 1 it is an elementwise L1 penalty.
        For 0 < l1_ratio < 1, the penalty is a combination of L1 and L2.

        .. versionadded:: 0.17
           Regularization parameter *l1_ratio* used in the Coordinate Descent
           solver.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_NMF_l1_ratio extends NumericRangeOptionPredicate {
        
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
    
