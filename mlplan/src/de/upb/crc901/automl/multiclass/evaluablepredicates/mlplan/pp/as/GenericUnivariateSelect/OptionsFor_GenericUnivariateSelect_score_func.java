
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GenericUnivariateSelect;
    /*
        score_func : callable
        Function taking two arrays X and y, and returning a pair of arrays
        (scores, pvalues). For modes 'percentile' or 'kbest' it can return
        a single array scores.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GenericUnivariateSelect_score_func extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.1;
        }

        @Override
        protected double getMax() {
            return 1;
        }

        @Override
        protected int getSteps() {
            return 2;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
