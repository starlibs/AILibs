
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.KernelPCA;
    /*
        degree : int, default=3
        Degree for poly kernels. Ignored by other kernels.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_KernelPCA_degree extends NumericRangeOptionPredicate {
        
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
    
