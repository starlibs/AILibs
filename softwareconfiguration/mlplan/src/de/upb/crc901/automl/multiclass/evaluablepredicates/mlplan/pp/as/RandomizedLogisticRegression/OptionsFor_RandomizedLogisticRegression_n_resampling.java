
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.RandomizedLogisticRegression;
    /*
        n_resampling : int, optional, default=200
        Number of randomized models.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_RandomizedLogisticRegression_n_resampling extends NumericRangeOptionPredicate {
        
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
    
