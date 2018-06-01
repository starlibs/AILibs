
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.as.MDS;
    /*
        eps : float, optional, default: 1e-3
        Relative tolerance with respect to stress at which to declare
        convergence.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_MDS_eps extends NumericRangeOptionPredicate {
        
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
    
