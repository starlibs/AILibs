
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.DictionaryLearning;
    /*
        max_iter : int,
        maximum number of iterations to perform


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_DictionaryLearning_max_iter extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 10;
        }

        @Override
        protected double getMax() {
            return 100;
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
    
