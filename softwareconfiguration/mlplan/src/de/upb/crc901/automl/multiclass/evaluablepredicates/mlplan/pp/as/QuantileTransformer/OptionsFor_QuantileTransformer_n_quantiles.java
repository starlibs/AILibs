
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.QuantileTransformer;
    /*
        n_quantiles : int, optional (default=1000)
        Number of quantiles to be computed. It corresponds to the number
        of landmarks used to discretize the cumulative density function.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_QuantileTransformer_n_quantiles extends NumericRangeOptionPredicate {
        
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
    
