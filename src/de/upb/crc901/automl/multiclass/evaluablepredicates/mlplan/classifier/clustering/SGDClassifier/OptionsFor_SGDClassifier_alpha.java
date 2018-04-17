
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.SGDClassifier;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        alpha : float
        Constant that multiplies the regularization term. Defaults to 0.0001
        Also used to compute learning_rate when set to 'optimal'.


    */
    public class OptionsFor_SGDClassifier_alpha extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 1
                ;
        }

        @Override
        protected double getMax() {
            return 1
                ;
        }

        @Override
        protected int getSteps() {
            return -1
                ;
        }

        @Override
        protected boolean needsIntegers() {
            return false; // already set by generator
        }

        @Override
        protected boolean isLinear() {
			return true;
		}
    }
    
