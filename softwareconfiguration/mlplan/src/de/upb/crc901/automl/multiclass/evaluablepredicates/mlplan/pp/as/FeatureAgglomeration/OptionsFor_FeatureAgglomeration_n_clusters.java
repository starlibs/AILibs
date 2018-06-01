
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.FeatureAgglomeration;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        n_clusters : int, default 2
        The number of clusters to find.


    */
    public class OptionsFor_FeatureAgglomeration_n_clusters extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return -1;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
        
        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
