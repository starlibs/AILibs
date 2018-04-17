
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.SparsePCA;
    /*
        alpha : float,
        Sparsity controlling parameter. Higher values lead to sparser
        components.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SparsePCA_alpha extends NumericRangeOptionPredicate {
        
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
    
