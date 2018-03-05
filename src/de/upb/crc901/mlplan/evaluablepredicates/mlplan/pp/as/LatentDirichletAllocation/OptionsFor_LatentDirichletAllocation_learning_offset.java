
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.LatentDirichletAllocation;
    /*
        learning_offset : float, optional (default=10.)
        A (positive) parameter that downweights early iterations in online
        learning.  It should be greater than 1.0. In the literature, this is
        called tau_0.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LatentDirichletAllocation_learning_offset extends NumericRangeOptionPredicate {
        
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
    
