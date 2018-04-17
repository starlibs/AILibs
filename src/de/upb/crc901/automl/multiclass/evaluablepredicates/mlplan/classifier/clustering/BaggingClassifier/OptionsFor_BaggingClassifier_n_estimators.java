
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.BaggingClassifier;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_estimators : int, optional (default=10)
        The number of base estimators in the ensemble.


    */
    public class OptionsFor_BaggingClassifier_n_estimators extends NumericRangeOptionPredicate {
        
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
    
