
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.LatentDirichletAllocation;
    /*
        learning_offset : float, optional (default=10.)
        A (positive) parameter that downweights early iterations in online
        learning.  It should be greater than 1.0. In the literature, this is
        called tau_0.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LatentDirichletAllocation_learning_offset extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 5;
        }

        @Override
        protected double getMax() {
            return 30;
        }

        @Override
        protected int getSteps() {
            return 4;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
