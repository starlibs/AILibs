
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.basic.SVC;
    /*
        C : float, optional (default=1.0)
        Penalty parameter C of the error term.


    */

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SVC_C extends NumericRangeOptionPredicate {
        
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
    
