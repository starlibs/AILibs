
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.SparsePCA;
    /*
        max_iter : int,
        Maximum number of iterations to perform.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SparsePCA_max_iter extends NumericRangeOptionPredicate {
        
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
    
