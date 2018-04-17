
    package de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.classifier.clustering.MLPClassifier;

    import de.upb.crc901.automl.multiclass.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    /*
        momentum : float, default 0.9
        Momentum for gradient descent update. Should be between 0 and 1. Only
        used when solver='sgd'.


    */
    public class OptionsFor_MLPClassifier_momentum extends NumericRangeOptionPredicate {
        
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
    
