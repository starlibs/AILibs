
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.BaggingClassifier;
    /*
        n_estimators : int, optional (default=10)
        The number of base estimators in the ensemble.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_BaggingClassifier_n_estimators extends NumericRangeOptionPredicate {
        
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
    
