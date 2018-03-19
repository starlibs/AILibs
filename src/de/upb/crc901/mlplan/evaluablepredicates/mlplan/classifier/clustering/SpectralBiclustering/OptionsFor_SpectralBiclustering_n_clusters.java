
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.SpectralBiclustering;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_clusters : integer or tuple (n_row_clusters, n_column_clusters)
        The number of row and column clusters in the checkerboard
        structure.


    */
    public class OptionsFor_SpectralBiclustering_n_clusters extends NumericRangeOptionPredicate {
        
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
    
