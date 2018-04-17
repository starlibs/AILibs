
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.SpectralBiclustering;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_best : integer, optional, default: 3
        Number of best singular vectors to which to project the data
        for clustering.


    */
    public class OptionsFor_SpectralBiclustering_n_best extends NumericRangeOptionPredicate {
        
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
    
