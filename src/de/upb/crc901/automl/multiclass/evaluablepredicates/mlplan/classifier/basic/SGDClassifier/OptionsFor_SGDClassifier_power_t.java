
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;
    /*
        power_t : double
        The exponent for inverse scaling learning rate [default 0.5].


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SGDClassifier_power_t extends NumericRangeOptionPredicate {
        
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
    
