
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SpectralClustering;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        coef0 : float, default=1
        Zero coefficient for polynomial and sigmoid kernels.
        Ignored by other kernels.


    */
    public class OptionsFor_SpectralClustering_coef0 extends NumericRangeOptionPredicate {
        
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
    
