
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.LogisticRegressionCV;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        max_iter : int, optional
        Maximum number of iterations of the optimization algorithm.


    */
    public class OptionsFor_LogisticRegressionCV_max_iter extends NumericRangeOptionPredicate {
        
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
    
