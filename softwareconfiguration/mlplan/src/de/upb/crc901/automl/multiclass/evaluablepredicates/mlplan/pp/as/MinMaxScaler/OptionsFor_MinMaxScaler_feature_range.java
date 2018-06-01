
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.MinMaxScaler;
    /*
        feature_range : tuple (min, max), default=(0, 1)
        Desired range of transformed data.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MinMaxScaler_feature_range extends NumericRangeOptionPredicate {
        
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
            return false;
        }
    }
    
