
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SpectralCoclustering;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_init : int, optional, default: 10
        Number of random initializations that are tried with the
        k-means algorithm.

        If mini-batch k-means is used, the best initialization is
        chosen and the algorithm runs once. Otherwise, the algorithm
        is run for each initialization and the best solution chosen.


    */
    public class OptionsFor_SpectralCoclustering_n_init extends NumericRangeOptionPredicate {
        
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
    
