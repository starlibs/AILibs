
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.TSNE;
    /*
        n_iter : int, optional (default: 1000)
        Maximum number of iterations for the optimization. Should be at
        least 250.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_TSNE_n_iter extends NumericRangeOptionPredicate {
        
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
    
