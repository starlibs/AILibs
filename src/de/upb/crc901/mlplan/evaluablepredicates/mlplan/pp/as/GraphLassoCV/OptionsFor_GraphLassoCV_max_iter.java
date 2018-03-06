
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.GraphLassoCV;
    /*
        max_iter : integer, optional
        Maximum number of iterations.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GraphLassoCV_max_iter extends NumericRangeOptionPredicate {
        
    	 
        @Override
        protected double getMin() {
            return 10;
        }

        @Override
        protected double getMax() {
            return 500;
        }

        @Override
        protected int getSteps() {
            return 4;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
