
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.RandomizedLogisticRegression;
    /*
        selection_threshold : float, optional, default=0.25
        The score above which features should be selected.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_RandomizedLogisticRegression_selection_threshold extends NumericRangeOptionPredicate {
        
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
    
