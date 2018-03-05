
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.SelectFwe;
    /*
        alpha : float, optional
        The highest uncorrected p-value for features to keep.

    Attributes
    
    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SelectFwe_alpha extends NumericRangeOptionPredicate {
        
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
    
