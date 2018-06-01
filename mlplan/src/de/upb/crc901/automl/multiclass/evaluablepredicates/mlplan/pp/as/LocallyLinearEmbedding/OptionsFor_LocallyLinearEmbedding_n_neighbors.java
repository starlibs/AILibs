
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.LocallyLinearEmbedding;
    /*
        n_neighbors : integer
        number of neighbors to consider for each point.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LocallyLinearEmbedding_n_neighbors extends NumericRangeOptionPredicate {
        
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
    
