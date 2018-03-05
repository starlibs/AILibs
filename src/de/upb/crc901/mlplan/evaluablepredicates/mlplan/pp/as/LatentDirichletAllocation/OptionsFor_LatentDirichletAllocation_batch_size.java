
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.LatentDirichletAllocation;
    /*
        batch_size : int, optional (default=128)
        Number of documents to use in each EM iteration. Only used in online
        learning.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LatentDirichletAllocation_batch_size extends NumericRangeOptionPredicate {
        
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
    }
    
