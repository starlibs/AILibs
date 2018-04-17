
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.CCA;
    /*
        max_iter : an integer, (default 500)
        the maximum number of iterations of the NIPALS inner loop


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_CCA_max_iter extends NumericRangeOptionPredicate {
        
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
    
