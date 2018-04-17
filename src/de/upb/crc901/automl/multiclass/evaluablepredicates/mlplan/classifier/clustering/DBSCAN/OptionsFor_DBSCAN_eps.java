
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.DBSCAN;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        eps : float, optional
        The maximum distance between two samples for them to be considered
        as in the same neighborhood.


    */
    public class OptionsFor_DBSCAN_eps extends NumericRangeOptionPredicate {
        
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
            return false; // already set by generator
        }

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
