
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.NMF;
    /*
        alpha : double, default: 0.
        Constant that multiplies the regularization terms. Set it to zero to
        have no regularization.

        .. versionadded:: 0.17
           *alpha* used in the Coordinate Descent solver.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_NMF_alpha extends NumericRangeOptionPredicate {
        
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
    
