
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.NuSVC;
    /*
        cache_size : float, optional
        Specify the size of the kernel cache (in MB).


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_NuSVC_cache_size extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 200;
        }

        @Override
        protected double getMax() {
            return 200;
        }

        @Override
        protected int getSteps() {
            return -1;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
