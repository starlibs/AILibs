
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.NuSVC;
    /*
        coef0 : float, optional (default=0.0)
        Independent term in kernel function.
        It is only significant in 'poly' and 'sigmoid'.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_NuSVC_coef0 extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 0.0;
        }

        @Override
        protected double getMax() {
            return 1000;
        }

        @Override
        protected int getSteps() {
            return 5;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
        
        @Override
        protected boolean isLinear() {
    		return false;
    	} 
    }
    
