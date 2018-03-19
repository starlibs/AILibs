
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.RandomForestClassifier;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_estimators : integer, optional (default=10)
        The number of trees in the forest.


    */
    public class OptionsFor_RandomForestClassifier_n_estimators extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1
                ;
        }

        @Override
        protected double getMax() {
            return 1
                ;
        }

        @Override
        protected int getSteps() {
            return -1
                ;
        }

        @Override
        protected boolean needsIntegers() {
            return true; // already set by generator
        }

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
