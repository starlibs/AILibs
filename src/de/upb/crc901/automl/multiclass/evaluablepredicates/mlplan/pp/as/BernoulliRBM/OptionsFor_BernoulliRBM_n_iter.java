
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.BernoulliRBM;
    /*
        n_iter : int, optional
        Number of iterations/sweeps over the training dataset to perform
        during training.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_BernoulliRBM_n_iter extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 10;
        }

        @Override
        protected int getSteps() {
            return 3;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
