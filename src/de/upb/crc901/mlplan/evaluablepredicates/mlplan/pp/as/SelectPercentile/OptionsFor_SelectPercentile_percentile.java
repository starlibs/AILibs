
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.SelectPercentile;
    /*
        percentile : int, optional, default=10
        Percent of features to keep.

    Attributes
    
    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SelectPercentile_percentile extends NumericRangeOptionPredicate {
        
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
    
