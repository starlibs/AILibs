
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.SpectralBiclustering;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_components : integer, optional, default: 6
        Number of singular vectors to check.


    */
    public class OptionsFor_SpectralBiclustering_n_components extends NumericRangeOptionPredicate {
        
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
    
