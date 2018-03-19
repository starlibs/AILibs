
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.pca.MiniBatchSparsePCA;
    /*
        n_jobs : int,
        number of parallel jobs to run, or -1 to autodetect.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MiniBatchSparsePCA_n_jobs extends NumericRangeOptionPredicate {
        
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
    
