
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.FactorAnalysis;
    /*
        tol : float
        Stopping tolerance for EM algorithm.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_FactorAnalysis_tol extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.1;
        }

        @Override
        protected double getMax() {
            return 0.5;
        }

        @Override
        protected int getSteps() {
            return 2;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
