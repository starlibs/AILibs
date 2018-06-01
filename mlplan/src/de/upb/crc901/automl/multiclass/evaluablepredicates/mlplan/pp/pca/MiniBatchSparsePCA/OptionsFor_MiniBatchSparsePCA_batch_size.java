
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.MiniBatchSparsePCA;
    /*
        batch_size : int,
        the number of features to take in each mini batch


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MiniBatchSparsePCA_batch_size extends NumericRangeOptionPredicate {
        
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
    
