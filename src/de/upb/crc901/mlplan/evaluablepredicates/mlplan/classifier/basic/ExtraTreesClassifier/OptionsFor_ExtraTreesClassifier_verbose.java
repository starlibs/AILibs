
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.ExtraTreesClassifier;
    /*
        verbose : int, optional (default=0)
        Controls the verbosity of the tree building process.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_ExtraTreesClassifier_verbose extends NumericRangeOptionPredicate {
        
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
    
