
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.RandomizedPCA;
    /*
        iterated_power : int, default=2
        Number of iterations for the power method.

        .. versionchanged:: 0.18


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_RandomizedPCA_iterated_power extends NumericRangeOptionPredicate {
        
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
    
