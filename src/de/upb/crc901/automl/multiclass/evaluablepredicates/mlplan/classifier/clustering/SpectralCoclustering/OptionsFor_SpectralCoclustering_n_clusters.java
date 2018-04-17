
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.SpectralCoclustering;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_clusters : integer, optional, default: 3
        The number of biclusters to find.


    */
    public class OptionsFor_SpectralCoclustering_n_clusters extends NumericRangeOptionPredicate {
        
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
    
