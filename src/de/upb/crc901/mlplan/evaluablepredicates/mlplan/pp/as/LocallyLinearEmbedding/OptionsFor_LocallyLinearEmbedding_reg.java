
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.LocallyLinearEmbedding;
    /*
        reg : float
        regularization constant, multiplies the trace of the local covariance
        matrix of the distances.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LocallyLinearEmbedding_reg extends NumericRangeOptionPredicate {
        
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
    
