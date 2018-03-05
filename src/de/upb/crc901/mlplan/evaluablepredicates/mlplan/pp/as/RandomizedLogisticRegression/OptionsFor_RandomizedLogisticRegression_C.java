
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.pp.as.RandomizedLogisticRegression;
    /*
        C : float or array-like of shape [n_reg_parameter], optional, default=1
        The regularization parameter C in the LogisticRegression.
        When C is an array, fit will take each regularization parameter in C
        one by one for LogisticRegression and store results for each one
        in ``all_scores_``, where columns and rows represent corresponding
        reg_parameters and features.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_RandomizedLogisticRegression_C extends NumericRangeOptionPredicate {
        
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
    
