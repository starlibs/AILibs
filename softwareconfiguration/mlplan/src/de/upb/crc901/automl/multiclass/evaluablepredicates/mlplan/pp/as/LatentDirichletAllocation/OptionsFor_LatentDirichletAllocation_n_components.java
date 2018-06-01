
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.LatentDirichletAllocation;
    /*
        n_components : int, optional (default=10)
        Number of topics.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LatentDirichletAllocation_n_components extends NumericRangeOptionPredicate {
        
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
    
