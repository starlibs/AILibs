
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.GraphLassoCV;
    /*
        n_jobs : int, optional
        number of jobs to run in parallel (default 1).


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GraphLassoCV_n_jobs extends NumericRangeOptionPredicate {
        
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
    
