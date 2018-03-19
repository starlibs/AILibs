
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.clustering.MiniBatchKMeans;

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        batch_size : int, optional, default: 100
        Size of the mini batches.


    */
    public class OptionsFor_MiniBatchKMeans_batch_size extends NumericRangeOptionPredicate {
        
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
    
