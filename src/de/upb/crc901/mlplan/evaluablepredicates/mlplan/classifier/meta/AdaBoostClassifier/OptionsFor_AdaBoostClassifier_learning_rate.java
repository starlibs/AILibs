
    package de.upb.crc901.mlplan.evaluablepredicates.mlplan.classifier.meta.AdaBoostClassifier;
    /*
        learning_rate : float, optional (default=1.)
        Learning rate shrinks the contribution of each classifier by
        ``learning_rate``. There is a trade-off between ``learning_rate`` and
        ``n_estimators``.


    */

    import de.upb.crc901.mlplan.evaluablepredicates.mlplan.NumericRangeOptionPredicate;

    public class OptionsFor_AdaBoostClassifier_learning_rate extends NumericRangeOptionPredicate {
        
        @Override
        protected double getMin() {
            return 10e-2;
        }

        @Override
        protected double getMax() {
            return 10e2;
        }

        @Override
        protected int getSteps() {
            return 5;
        }
        
        protected boolean isLinear() {
        	return false;
        }

        @Override
        protected boolean needsIntegers() {
            return false;
        }
    }
    
