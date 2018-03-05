
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.RandomizedLogisticRegression;
    /*
        sample_fraction : float, optional, default=0.75
        The fraction of samples to be used in each randomized design.
        Should be between 0 and 1. If 1, all samples are used.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_RandomizedLogisticRegression_sample_fraction extends NumericRangeOptionPredicate {
        
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
    
