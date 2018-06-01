
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GraphLassoCV;
    /*
        n_jobs : int, optional
        number of jobs to run in parallel (default 1).


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GraphLassoCV_n_jobs extends NumericRangeOptionPredicate {
        
    	 
        @Override
        protected double getMin() {
            return 2;
        }

        @Override
        protected double getMax() {
            return 4;
        }

        @Override
        protected int getSteps() {
            return 1;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
