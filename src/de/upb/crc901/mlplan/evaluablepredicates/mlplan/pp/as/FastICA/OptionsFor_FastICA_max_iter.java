
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.FastICA;
    /*
        max_iter : int, optional
        Maximum number of iterations during fit.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_FastICA_max_iter extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 10;
        }

        @Override
        protected double getMax() {
            return 50;
        }

        @Override
        protected int getSteps() {
            return 2;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
