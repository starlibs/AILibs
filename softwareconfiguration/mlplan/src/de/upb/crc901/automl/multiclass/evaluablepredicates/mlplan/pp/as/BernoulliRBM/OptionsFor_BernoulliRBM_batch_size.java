
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.BernoulliRBM;
    /*
        batch_size : int, optional
        Number of examples per minibatch.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_BernoulliRBM_batch_size extends NumericRangeOptionPredicate {
        

        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 3;
        }

        @Override
        protected int getSteps() {
            return 2;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
