
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.MiniBatchSparsePCA;
    /*
        n_iter : int,
        number of iterations to perform for each mini batch


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MiniBatchSparsePCA_n_iter extends NumericRangeOptionPredicate {
        
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
    
