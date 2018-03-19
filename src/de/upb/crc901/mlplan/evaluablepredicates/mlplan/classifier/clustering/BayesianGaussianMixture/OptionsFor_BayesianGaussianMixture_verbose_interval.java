
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.BayesianGaussianMixture;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        verbose_interval : int, default to 10.
        Number of iteration done before the next print.

    Attributes
    
    */
    public class OptionsFor_BayesianGaussianMixture_verbose_interval extends NumericRangeOptionPredicate {
        
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
    
