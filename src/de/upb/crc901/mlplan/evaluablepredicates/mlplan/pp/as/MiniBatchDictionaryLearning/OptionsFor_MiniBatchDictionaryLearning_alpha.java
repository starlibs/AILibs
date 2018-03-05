
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.MiniBatchDictionaryLearning;
    /*
        alpha : float,
        sparsity controlling parameter


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MiniBatchDictionaryLearning_alpha extends NumericRangeOptionPredicate {
        
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
    
