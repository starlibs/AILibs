
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.GraphLasso;
    /*
        alpha : positive float, default 0.01
        The regularization parameter: the higher alpha, the more
        regularization, the sparser the inverse covariance.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_GraphLasso_alpha extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.01;
        }

        @Override
        protected double getMax() {
            return 0.5;
        }

        @Override
        protected int getSteps() {
            return 5;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
