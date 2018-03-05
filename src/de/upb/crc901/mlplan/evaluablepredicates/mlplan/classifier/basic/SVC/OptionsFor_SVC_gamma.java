package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.SVC;

    import java.util.Arrays;
    import java.util.List;

import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;
import de.upb.crc901.mlplan.evaluablepredicates.mlplan.OptionsPredicate;
    /*
        gamma : float, optional (default='auto')
        Kernel coefficient for 'rbf', 'poly' and 'sigmoid'.
        If gamma is 'auto' then 1/n_features will be used instead.


    */
    public class OptionsFor_SVC_gamma extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 10e-5;
        }

        @Override
        protected double getMax() {
            return 10e4;
        }

        @Override
        protected int getSteps() {
            return 10;
        }
        
        protected boolean isLinear() {
        	return false;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
