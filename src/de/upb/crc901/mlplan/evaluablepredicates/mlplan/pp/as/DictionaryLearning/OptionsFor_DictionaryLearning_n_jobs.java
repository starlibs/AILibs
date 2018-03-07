
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.DictionaryLearning;
    /*
        n_jobs : int,
        number of parallel jobs to run


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_DictionaryLearning_n_jobs extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 4;
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
    
