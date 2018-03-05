
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;
    /*
        l1_ratio : float
        The Elastic Net mixing parameter, with 0 <= l1_ratio <= 1.
        l1_ratio=0 corresponds to L2 penalty, l1_ratio=1 to L1.
        Defaults to 0.15.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SGDClassifier_l1_ratio extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return 10;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
