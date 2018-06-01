
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.pp.pca.KernelPCA;
    /*
        n_jobs : int, default=1
        The number of parallel jobs to run.
        If `-1`, then the number of jobs is set to the number of CPU cores.

        .. versionadded:: 0.18

    Attributes
    
    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_KernelPCA_n_jobs extends NumericRangeOptionPredicate {
        
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
    
