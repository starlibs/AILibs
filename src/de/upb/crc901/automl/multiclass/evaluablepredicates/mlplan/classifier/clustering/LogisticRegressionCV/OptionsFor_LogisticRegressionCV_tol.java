
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.LogisticRegressionCV;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        tol : float, optional
        Tolerance for stopping criteria.


    */
    public class OptionsFor_LogisticRegressionCV_tol extends NumericRangeOptionPredicate {
        
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
            return false; // already set by generator
        }

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
