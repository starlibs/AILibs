
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SpectralClustering;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        degree : float, default=3
        Degree of the polynomial kernel. Ignored by other kernels.


    */
    public class OptionsFor_SpectralClustering_degree extends NumericRangeOptionPredicate {
        
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
    
