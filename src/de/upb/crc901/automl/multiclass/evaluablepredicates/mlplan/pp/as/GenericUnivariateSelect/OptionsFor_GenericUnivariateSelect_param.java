
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GenericUnivariateSelect;
    /*
        param : float or int depending on the feature selection mode
        Parameter of the corresponding mode.

    Attributes
    
    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GenericUnivariateSelect_param extends NumericRangeOptionPredicate {
        
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
    
