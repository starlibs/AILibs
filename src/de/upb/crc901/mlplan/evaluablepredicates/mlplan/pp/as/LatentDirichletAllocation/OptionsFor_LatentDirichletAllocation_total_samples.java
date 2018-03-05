
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.LatentDirichletAllocation;
    /*
        total_samples : int, optional (default=1e6)
        Total number of documents. Only used in the `partial_fit` method.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LatentDirichletAllocation_total_samples extends NumericRangeOptionPredicate {
        
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
            return false;
        }
    }
    
