
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.KMeans;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        max_iter : int, default: 300
        Maximum number of iterations of the k-means algorithm for a
        single run.


    */
    public class OptionsFor_KMeans_max_iter extends NumericRangeOptionPredicate {
        
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
    
