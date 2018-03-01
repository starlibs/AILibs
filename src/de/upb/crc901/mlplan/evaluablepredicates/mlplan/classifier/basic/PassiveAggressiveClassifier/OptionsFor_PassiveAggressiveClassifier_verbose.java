
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.PassiveAggressiveClassifier;
    /*
        verbose : integer, optional
        The verbosity level


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_PassiveAggressiveClassifier_verbose extends NumericRangeOptionPredicate {
        
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
    
