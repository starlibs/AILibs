
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.SelectKBest;
    /*
        k : int or "all", optional, default=10
        Number of top features to select.
        The "all" option bypasses selection, for use in a parameter search.

    Attributes
    
    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SelectKBest_k extends NumericRangeOptionPredicate {
        
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
    
