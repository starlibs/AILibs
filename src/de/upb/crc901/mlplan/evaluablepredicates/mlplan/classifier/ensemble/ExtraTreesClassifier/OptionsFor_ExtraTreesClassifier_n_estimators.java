
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.ensemble.ExtraTreesClassifier;
    /*
        n_estimators : integer, optional (default=10)
        The number of trees in the forest.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_ExtraTreesClassifier_n_estimators extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 1000;
        }

        @Override
        protected int getSteps() {
            return 10;
        }
        
        protected boolean isLinear() {
        	return false;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
