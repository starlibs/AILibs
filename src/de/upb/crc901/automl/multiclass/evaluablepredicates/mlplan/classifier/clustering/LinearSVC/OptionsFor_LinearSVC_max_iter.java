
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.LinearSVC;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        max_iter : int, (default=1000)
        The maximum number of iterations to be run.

    Attributes
    
    */
    public class OptionsFor_LinearSVC_max_iter extends NumericRangeOptionPredicate {
        
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
    
