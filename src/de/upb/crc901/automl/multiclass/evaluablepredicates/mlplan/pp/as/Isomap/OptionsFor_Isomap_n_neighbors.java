
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.Isomap;
    /*
        n_neighbors : integer
        number of neighbors to consider for each point.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_Isomap_n_neighbors extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 5;
        }

        @Override
        protected double getMax() {
            return 10;
        }

        @Override
        protected int getSteps() {
            return 2;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
