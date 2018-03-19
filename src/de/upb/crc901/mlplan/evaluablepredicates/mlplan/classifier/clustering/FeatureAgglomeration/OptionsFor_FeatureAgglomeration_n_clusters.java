
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.FeatureAgglomeration;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_clusters : int, default 2
        The number of clusters to find.


    */
    public class OptionsFor_FeatureAgglomeration_n_clusters extends NumericRangeOptionPredicate {
        
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
    
