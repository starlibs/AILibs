
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.NuSVC;
    /*
        degree : int, optional (default=3)
        Degree of the polynomial kernel function ('poly').
        Ignored by all other kernels.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_NuSVC_degree extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 3;
        }

        @Override
        protected double getMax() {
            return 7;
        }

        @Override
        protected int getSteps() {
            return 1;
        }

        @Override
        protected boolean needsIntegers() {
            return true;
        }
    }
    
