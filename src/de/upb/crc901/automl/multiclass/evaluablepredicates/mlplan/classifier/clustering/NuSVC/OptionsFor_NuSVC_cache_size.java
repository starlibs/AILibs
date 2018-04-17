
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.NuSVC;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        cache_size : float, optional
        Specify the size of the kernel cache (in MB).


    */
    public class OptionsFor_NuSVC_cache_size extends NumericRangeOptionPredicate {
        
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
    
