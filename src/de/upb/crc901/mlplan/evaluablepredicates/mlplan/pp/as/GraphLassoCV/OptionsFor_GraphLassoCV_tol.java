
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.GraphLassoCV;
    /*
        tol : positive float, optional
        The tolerance to declare convergence: if the dual gap goes below
        this value, iterations are stopped.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GraphLassoCV_tol extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.1;
        }

        @Override
        protected double getMax() {
            return 2;
        }

        @Override
        protected int getSteps() {
            return 5;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
