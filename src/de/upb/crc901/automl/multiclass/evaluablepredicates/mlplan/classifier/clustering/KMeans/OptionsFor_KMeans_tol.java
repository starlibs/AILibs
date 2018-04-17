
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.KMeans;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        tol : float, default: 1e-4
        Relative tolerance with regards to inertia to declare convergence


    */
    public class OptionsFor_KMeans_tol extends NumericRangeOptionPredicate {
        
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
    
