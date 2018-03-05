
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.BernoulliRBM;
    /*
        batch_size : int, optional
        Number of examples per minibatch.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_BernoulliRBM_batch_size extends NumericRangeOptionPredicate {
        
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
    
