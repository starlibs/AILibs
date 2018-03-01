
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.MLPClassifier;
    /*
        alpha : float, optional, default 0.0001
        L2 penalty (regularization term) parameter.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MLPClassifier_alpha extends NumericRangeOptionPredicate {
        
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
    
