
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.BayesianGaussianMixture;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        max_iter : int, defaults to 100.
        The number of EM iterations to perform.


    */
    public class OptionsFor_BayesianGaussianMixture_max_iter extends NumericRangeOptionPredicate {
        
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
    
