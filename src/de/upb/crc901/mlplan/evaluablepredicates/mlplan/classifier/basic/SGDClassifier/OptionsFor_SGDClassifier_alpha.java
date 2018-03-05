
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.basic.SGDClassifier;
    /*
        alpha : float
        Constant that multiplies the regularization term. Defaults to 0.0001
        Also used to compute learning_rate when set to 'optimal'.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_SGDClassifier_alpha extends NumericRangeOptionPredicate {
        
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
    
