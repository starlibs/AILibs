
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.DictionaryLearning;
    /*
        tol : float,
        tolerance for numerical error


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_DictionaryLearning_tol extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.01;
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
    
