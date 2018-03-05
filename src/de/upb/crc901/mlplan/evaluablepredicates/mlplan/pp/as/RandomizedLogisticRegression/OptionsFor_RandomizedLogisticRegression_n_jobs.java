
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.RandomizedLogisticRegression;
    /*
        n_jobs : integer, optional
        Number of CPUs to use during the resampling. If '-1', use
        all the CPUs


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_RandomizedLogisticRegression_n_jobs extends NumericRangeOptionPredicate {
        
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
    
