
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.FastICA;
    /*
        tol : float, optional
        Tolerance on update at each iteration.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_FastICA_tol extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.1;
        }

        @Override
        protected double getMax() {
            return 1.;
        }

        @Override
        protected int getSteps() {
            return 3;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
