
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.FactorAnalysis;
    /*
        iterated_power : int, optional
        Number of iterations for the power method. 3 by default. Only used
        if ``svd_method`` equals 'randomized'


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_FactorAnalysis_iterated_power extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 2;
        }

        @Override
        protected double getMax() {
            return 6;
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
    
