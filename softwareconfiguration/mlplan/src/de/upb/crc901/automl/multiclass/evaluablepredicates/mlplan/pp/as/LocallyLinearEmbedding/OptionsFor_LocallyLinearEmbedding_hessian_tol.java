
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.LocallyLinearEmbedding;
    /*
        hessian_tol : float, optional
        Tolerance for Hessian eigenmapping method.
        Only used if ``method == 'hessian'``


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_LocallyLinearEmbedding_hessian_tol extends NumericRangeOptionPredicate {
        
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
    
